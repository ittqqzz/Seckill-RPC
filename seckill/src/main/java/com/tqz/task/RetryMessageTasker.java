package com.tqz.task;

import com.tqz.dao.BrokerMessageLogDao;
import com.tqz.entity.BrokerMessageLog;
import com.tqz.entity.User;
import com.tqz.enums.ConstantEnum;
import com.tqz.mq.RabbitSuccessKilledSender;
import com.tqz.utils.FastJsonConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class RetryMessageTasker {

    @Autowired
    private RabbitSuccessKilledSender rabbitSuccessKilledSender;

    @Autowired
    private BrokerMessageLogDao brokerMessageLogDao;

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void reSend(){
        System.out.println("-----------定时任务开始-----------");
        //pull status = 0 and timeout message
        List<BrokerMessageLog> list = brokerMessageLogDao.query4StatusAndTimeoutMessage();
        list.forEach(messageLog -> {
            if(messageLog.getTryCount() >= 3){
                //update fail message, 例如交换机、队列绑定错误就属于
                brokerMessageLogDao.changeBrokerMessageLogStatus(messageLog.getMessageId(), ConstantEnum.SUCCESS_KILLED_SEND_FAILURE.getStatus(), new Date());
                System.out.println("存在秒杀成功用户未被消费");
            } else {
                // resend
                // 定时任务发现存在没有成功到达交换机的 MQ ，开始重试
                brokerMessageLogDao.update4ReSend(messageLog.getMessageId(), new Date());
                User reSendUser = FastJsonConvertUtil.convertJSONToObject(messageLog.getMessage(), User.class);
                System.out.println("重新发送用户：" + reSendUser.getPhone() + " 到MQ中");
                try {
                    rabbitSuccessKilledSender.sendSuccessKilledUserInfo(reSendUser);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("-----------定时任务异常-----------");
                }
            }
        });
    }
}

