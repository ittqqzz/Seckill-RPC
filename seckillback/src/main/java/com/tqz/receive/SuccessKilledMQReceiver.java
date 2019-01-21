package com.tqz.receive;

import com.rabbitmq.client.Channel;
import com.tqz.dto.SeckillExecution;
import com.tqz.entity.User;
import com.tqz.serivce.SuccessKilledService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 【注意】在序列化与反序列化中，包结构要一致，不然会出现无法序列化找不到实体类的错误
 */
@Component
public class SuccessKilledMQReceiver {

    @Autowired
    private SuccessKilledService successKilledService;


    //配置监听的哪一个队列，同时在没有queue和exchange的情况下会去创建并建立绑定关系
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "successkilled-queue", durable = "true"),
            exchange = @Exchange(name = "successkilled-exchange", durable = "true", type = "topic"),
            key = "successkilled.*"
    )
    )
    @RabbitHandler//如果有消息过来，在消费的时候调用这个方法
    public void onOrderMessage(@Payload User user,
                               @Headers Map<String, Object> headers,
                               Channel channel, Message message) throws IOException {

        //消费者操作
        System.out.println("---------收到消息，开始消费---------");
        System.out.println("用户Phone：" + user.getPhone());
        System.out.println("商品ID：" + user.getSeckillId());

        successKilledService.executeSeckill(user);

        /**
         * Delivery Tag 用来标识信道中投递的消息。RabbitMQ 推送消息给 Consumer 时，会附带一个 Delivery Tag，
         * 以便 Consumer 可以在消息确认时告诉 RabbitMQ 到底是哪条消息被确认了。
         * RabbitMQ 保证在每个信道中，每条消息的 Delivery Tag 从 1 开始递增。
         */
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        /**
         *  multiple 取值为 false 时，表示通知 RabbitMQ 当前消息被确认
         *  如果为 true，则额外将比第一个参数指定的 delivery tag 小的消息一并确认
         */
        boolean multiple = false;

        //ACK,确认一条消息已经被消费
        // 如果不写这句话的话，消息被消费后不会从队列里面删除
        channel.basicAck(deliveryTag, multiple);
        /**
         * deliveryTag（唯一标识 ID）
         * multiple：为了减少网络流量，手动确认可以被批处理，
         * 当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
         */
    }
}
