package com.tqz.mq;

import com.tqz.dao.BrokerMessageLogDao;
import com.tqz.entity.User;
import com.tqz.enums.ConstantEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MQ 如何发消息？
 * 1. 用户秒杀请求到达下游后，获取用户信息即可（即我只需要知道谁成功秒杀了）
 * 2. 将用户信息投递到 MQ 中，同时往broker表里面写入记录
 *
 *
 * MQ 如何接收消息？
 * 1. 监听消息队列，正常情况下监听到并可以正确处理
 *      并写入 successkilled 表，且更新 broker 表
 * 2. 如果异常，MQ 的消息无法到达消费端，则定时任务会轮询broker表，将未消费成功的消息重发
 */

public class RabbitSuccessKilledSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BrokerMessageLogDao brokerMessageLogDao;

    //回调函数: confirm确认
    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            System.err.println("correlationData: " + correlationData);
            String messageId = correlationData.getId();
            if(ack){
                //如果confirm返回成功 则进行更新
                // MQ成功到达交换机
                brokerMessageLogDao.changeBrokerMessageLogStatus(messageId, ConstantEnum.SUCCESS_KILLED_SEND_SUCCESS.getStatus(), new Date());
                System.out.println("MQ消息成功到达Exchange");
            } else {
                //失败则进行具体的后续操作:重试 或者补偿等手段
                // MQ 没有成功到达交换机，定时任务会检查 broker 的 status 然后重试
                System.err.println("MQ消息到达Exchange异常...");
            }
        }
    };

    //发送消息方法调用: 构建自定义对象消息
    public void sendSuccessKilledUserInfo(User user) throws Exception {
        /**
         * 通过实现 ConfirmCallback 接口，
         * 消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达 Exchange 中
         */
        rabbitTemplate.setConfirmCallback(confirmCallback);
        // 消息唯一ID
        CorrelationData correlationData = new CorrelationData(user.getPhone());
        // 发送 MQ ，发送完毕后会触发回调检查本次是否发送成功
        rabbitTemplate.convertAndSend("successkilled-exchange", "successkilled.sender", user, correlationData);
    }
}
