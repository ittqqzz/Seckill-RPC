package com.tqz.web;

import com.tqz.dao.BrokerMessageLogDao;
import com.tqz.dto.Exposer;
import com.tqz.dto.SeckillExecution;
import com.tqz.dto.SeckillResult;
import com.tqz.entity.BrokerMessageLog;
import com.tqz.entity.Seckill;
import com.tqz.enums.SeckillStateEnum;
import com.tqz.exception.RepeatKillException;
import com.tqz.exception.SeckillCloseException;
import com.tqz.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Component
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> list = seckillService.getSeckillList(0, 4);
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    /**
     * 进入detail页面后，js先自动请求一把，看一下秒杀是否开始了
     * 后端判断：
     *      若秒杀开始了，则返回Exposer(true, md5, seckillId);前端拿到数据开始封装秒杀url：seckill.URL.execution(seckillId, md5);
     *
     *      若没有开始，则返回Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());并启动倒计时
     * 若计时结束，再次自动触发此链接
     */
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            e.printStackTrace();
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    /**
     * MD5主要是用来拦截用户非法请求，例如用户猜到了秒杀连接，他可以/10/execution从而违规秒杀，
     * 但是用一个md5过滤一下，即使他猜到了连接，由于不知道具体的加密过程，他还是无法秒杀
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone", required = false) String userPhone) {
        if (userPhone == null) {
            return new SeckillResult(false, "未注册");
        } else {
            try {

                SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
                return new SeckillResult(true, execution);

            } catch (RepeatKillException e1) {
                SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
                return new SeckillResult(true, execution);
            } catch (SeckillCloseException e2) {
                SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
                return new SeckillResult(true, execution);
            } catch (Exception e) {
                SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
                return new SeckillResult(true, execution);
            }
        }
    }

    /**
     * 为什么要单独地获取系统时间？
     * 答：
     */
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }

    @Autowired
    private BrokerMessageLogDao brokerMessageLogDao;

    @RequestMapping("/test")
    public void test() throws Exception{
//        User user = new User();
//        user.setPhone("13886666666");
//        successKilledService.createSuccessKilledMQMsg(user);

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























