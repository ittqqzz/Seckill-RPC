package com.tqz.service.impl;

import com.tqz.dao.BrokerMessageLogDao;
import com.tqz.entity.BrokerMessageLog;
import com.tqz.entity.User;
import com.tqz.enums.ConstantEnum;
import com.tqz.exception.RepeatKillException;
import com.tqz.mq.RabbitSuccessKilledSender;
import com.tqz.service.ReadyMqService;
import com.tqz.utils.DateUtils;
import com.tqz.utils.FastJsonConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReadyMqServiceImpl implements ReadyMqService {

    @Autowired
    private BrokerMessageLogDao brokerMessageLogDao;

    @Autowired
    private RabbitSuccessKilledSender rabbitSuccessKilledSender;

    /**
     * 告知 MQ 哪位用户秒杀成功
     * @param user
     * @throws Exception
     */
    @Override
    public void createSuccessKilledMQMsg(User user) throws Exception{
        // 使用当前时间当做successKilledTime创建时间（为了模拟一下简化）
        Date successKilledTime = new Date();

        // 插入消息记录表数据
        BrokerMessageLog brokerMessageLog = new BrokerMessageLog();
        // 消息唯一ID
        brokerMessageLog.setMessageId(user.getPhone());
        // 保存消息整体 转为JSON 格式存储入库
        brokerMessageLog.setMessage(FastJsonConvertUtil.convertObjectToJSON(user));

        // 设置消息状态为0 表示发送中
        brokerMessageLog.setStatus("0");
        // 设置消息未确认超时时间窗口为 一分钟
        brokerMessageLog.setNextRetry(DateUtils.addMinutes(successKilledTime, ConstantEnum.SUCCESS_KILLED_TIMEOUT));
        brokerMessageLog.setCreateTime(new Date());
        brokerMessageLog.setUpdateTime(new Date());

        // TODO 抛异常时会回滚数据，导致该库无数据写入
        // 需要判断messageid 与 status
        int insertCount = brokerMessageLogDao.insertSelective(brokerMessageLog);
        if (insertCount <= 0) {
            throw new RepeatKillException("seckill repeated");
        }
        // 发送消息
        rabbitSuccessKilledSender.sendSuccessKilledUserInfo(user);
    }

}
