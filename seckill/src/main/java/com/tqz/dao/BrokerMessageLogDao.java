package com.tqz.dao;

import com.tqz.entity.BrokerMessageLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public interface BrokerMessageLogDao {

    /**
     * 查询消息状态为0(发送中) 且已经超时的消息集合
     * @return
     */
    List<BrokerMessageLog> query4StatusAndTimeoutMessage();

    /**
     * 重新发送统计count发送次数 +1
     * @param messageId
     * @param updateTime
     */
    void update4ReSend(@Param("messageId")String messageId, @Param("updateTime") Date updateTime);
    /**
     * 更新最终消息发送结果 成功 or 失败
     * @param messageId
     * @param status
     * @param updateTime
     */
    void changeBrokerMessageLogStatus(@Param("messageId")String messageId, @Param("status")String status, @Param("updateTime")Date updateTime);

    /**
     * 前端 插入秒杀记录
     * @param record
     * @return
     */
    int insertSelective(BrokerMessageLog record);
}
