package com.tqz.dao;

import com.tqz.entity.BrokerMessageLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class BrokerMessageLogDaoTest {

    @Autowired
    private BrokerMessageLogDao brokerMessageLogDao;

    @Test
    public void query4StatusAndTimeoutMessage() {
    }

    @Test
    public void update4ReSend() {
    }

    @Test
    public void changeBrokerMessageLogStatus() {
    }

    @Test
    public void insertSelective() {
        BrokerMessageLog brokerMessageLog = new BrokerMessageLog();
        brokerMessageLog.setCreateTime(new Date());
        brokerMessageLog.setNextRetry(new Date());
        brokerMessageLog.setUpdateTime(new Date());
        brokerMessageLog.setMessage("broker 测试");
        brokerMessageLog.setMessageId("123456789");
        brokerMessageLog.setStatus("0");
        brokerMessageLog.setTryCount(0);
        brokerMessageLogDao.insertSelective(brokerMessageLog);
    }
}