package com.tqz.serivce;

import com.tqz.dao.SeckillDao;
import com.tqz.dao.SuccessKilledDao;
import com.tqz.dto.SeckillExecution;
import com.tqz.entity.SuccessKilled;
import com.tqz.entity.User;
import com.tqz.enums.SeckillStateEnum;
import com.tqz.exception.RepeatKillException;
import com.tqz.exception.SeckillCloseException;
import com.tqz.exception.SeckillException;
import com.tqz.serivce.impl.ISecKillServiceAPIImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SuccessKilledService {

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private SeckillDao seckillDao;

    // todo 事务管理
    @Transactional
    public void executeSeckill(User user) {
        /**
         * 1. 先插入秒杀成功
         * 2. 再减库存
         */
        Date nowTime = new Date();
        String seckillId_userPhone = user.getSeckillId() + user.getPhone();
        try {
            // 先增加明细
            int insertCount = successKilledDao.insertSuccessKilled(user.getSeckillId(), user.getPhone(), user.getPhone());
            // 再看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                // 重复秒杀会返回0，就需要抛异常，同时存入秒杀状态到 map
                ISecKillServiceAPIImpl.setSeckillStatusToMap(seckillId_userPhone,
                        new SeckillExecution(user.getSeckillId(), SeckillStateEnum.REPEAT_KILL));
                throw new RepeatKillException("seckill repeated");
            } else {

                // 然后再减库存
                int updateCount = seckillDao.reduceNumber(user.getSeckillId(), nowTime);
                if (updateCount <= 0) {
                    // 没有更新库存记录【库存减到零了】，说明秒杀结束 rollback
                    ISecKillServiceAPIImpl.setSeckillStatusToMap(seckillId_userPhone,
                            new SeckillExecution(user.getSeckillId(), SeckillStateEnum.END));

                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(user.getSeckillId(), user.getPhone());
                    ISecKillServiceAPIImpl.setSeckillStatusToMap(seckillId_userPhone,
                            new SeckillExecution(user.getSeckillId(), SeckillStateEnum.SUCCESS, successKilled));
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            ISecKillServiceAPIImpl.setSeckillStatusToMap(seckillId_userPhone,
                    new SeckillExecution(user.getSeckillId(), SeckillStateEnum.INNER_ERROR));
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }
}
