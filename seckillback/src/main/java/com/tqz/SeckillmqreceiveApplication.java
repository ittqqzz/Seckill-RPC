package com.tqz;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableDubbo
@SpringBootApplication
@MapperScan("com.tqz.dao")
@EnableCaching
public class SeckillmqreceiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillmqreceiveApplication.class, args);
    }

}

