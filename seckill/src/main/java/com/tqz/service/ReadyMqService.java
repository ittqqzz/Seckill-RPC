package com.tqz.service;

import com.tqz.entity.User;

public interface ReadyMqService {

    void createSuccessKilledMQMsg(User user) throws Exception;
}
