package com.tqz.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SuccessKilledDaoTest {

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        long seckillId = 1000L;
        String userPhone="13556666666";
        int insertCount=successKilledDao.insertSuccessKilled(seckillId, userPhone, userPhone);
        System.out.println("insertCount=============" + insertCount);
    }

    @Test
    public void queryByIdWithSeckill() {
    }
}