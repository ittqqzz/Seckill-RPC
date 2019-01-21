package com.tqz.mq;

import com.tqz.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-rabbitmq-send.xml"})
public class SuccessKilledHandlerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendTest() {
        User user = new User();
        user.setPhone("12336665565");
        CorrelationData correlationData = new CorrelationData();
        rabbitTemplate.convertAndSend("successkilled-exchange",
                "successkilled.abcd", user, correlationData);

    }
}