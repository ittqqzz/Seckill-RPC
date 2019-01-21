package com.tqz.service.impl;

import com.tqz.dao.cache.RedisDao;
import com.tqz.dto.Exposer;
import com.tqz.dto.SeckillExecution;
import com.tqz.dto.SeckillResult;
import com.tqz.entity.Seckill;
import com.tqz.entity.SuccessKilled;
import com.tqz.entity.User;
import com.tqz.enums.SeckillStateEnum;
import com.tqz.exception.RepeatKillException;
import com.tqz.exception.SeckillCloseException;
import com.tqz.exception.SeckillException;
import com.tqz.service.ISecKillServiceAPI;
import com.tqz.service.SeckillService;
import com.tqz.service.ReadyMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SALT = "f5e6g3v2xs66u9#6%ser53@d35[,.";

    @Autowired
    private ReadyMqService readyMqService;

    @Autowired
    private RedisDao redisDao;

    /**
     * 使用 dubbo 消费服务
     */
    @Autowired
    private ISecKillServiceAPI iSecKillServiceAPI; // 这个发红不用管，RPC代理注入是成功的

    @Override
    public List<Seckill> getSeckillList(int offset, int limit) {
        List<Seckill> seckillList = redisDao.getSeckillList(offset, limit);
        if (seckillList == null) {
            seckillList = iSecKillServiceAPI.queryAll(offset, limit);
            redisDao.putSeckillList(seckillList, offset, limit);
        }
        return seckillList;
    }

    @Override
    public Seckill getById(long seckillId) {
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            seckill = iSecKillServiceAPI.queryById(seckillId);
            redisDao.putSeckill(seckill);
        }
        return seckill;
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = getById(seckillId);
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + SALT;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, String userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            // 秒杀数据被重写了，用户违规操作，抛异常！
            return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        Date nowTime = new Date();
        /**
         * 发送 MQ
         */
        try {

            readyMqService.createSuccessKilledMQMsg(new User(userPhone, seckillId));

            System.out.println("======== After sendToExchange start to query seckill status ==========");
            /**
             * TODO RPC远程读取秒杀状态是否合理？
             * 用redis缓存秒杀状态？
             */
            SeckillExecution seckillExecution = null;
            while (seckillExecution == null) {
                seckillExecution = iSecKillServiceAPI.querySekillStatus(seckillId + userPhone);
            }
            return seckillExecution;
        } catch (RepeatKillException e1) {
            throw new RepeatKillException("seckill repeated");
        } catch (SeckillCloseException e2) {
            throw new SeckillCloseException("seckill closed");
        }catch (Exception e) {
            e.printStackTrace();
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }
}







