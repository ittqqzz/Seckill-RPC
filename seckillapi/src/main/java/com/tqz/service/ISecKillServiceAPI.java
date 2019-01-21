package com.tqz.service;

import com.tqz.dto.SeckillExecution;
import com.tqz.entity.Seckill;
import com.tqz.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 给 seckill 模块访问 seckillback 使用的 RPC接口
 */
public interface ISecKillServiceAPI {

    /**
     * 根据id查询秒杀的商品信息
     *
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀商品列表
     *
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    SuccessKilled queryByIdWithSeckill(long seckillId, String userPhone);

    /**
     * RPC远程查询秒杀状态
     * @param seckillId_userPhone
     * @return
     */
    SeckillExecution querySekillStatus(String seckillId_userPhone);

}
