package com.tqz.serivce.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.tqz.dao.SeckillDao;
import com.tqz.dao.SuccessKilledDao;
import com.tqz.dto.SeckillExecution;
import com.tqz.entity.Seckill;
import com.tqz.entity.SuccessKilled;
import com.tqz.service.ISecKillServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Service(interfaceClass = ISecKillServiceAPI.class)// 暴露服务
public class ISecKillServiceAPIImpl implements ISecKillServiceAPI {

    // todo 不合理，因该用redis缓存此状态, 还要设置过期时间
    private static Map<String, SeckillExecution> seckillStatusMap = new ConcurrentHashMap<>(5);

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Override
    public Seckill queryById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public List<Seckill> queryAll(int offset, int limit) {
        return seckillDao.queryAll(offset, limit);
    }

    @Override
    public SuccessKilled queryByIdWithSeckill(long seckillId, String userPhone) {
        return successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
    }

    @Override
    public SeckillExecution querySekillStatus(String seckillId_userPhone) {
        return seckillStatusMap.get(seckillId_userPhone);
    }

    public static void setSeckillStatusToMap(String seckillId_userPhone, SeckillExecution seckillExecution) {
        seckillStatusMap.put(seckillId_userPhone, seckillExecution);
    }
}
