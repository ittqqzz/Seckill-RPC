package com.tqz.entity;

import java.util.List;

public class CacheEntry {

    private List<Seckill> seckillList;

    public CacheEntry(List<Seckill> seckillList) {
        this.seckillList = seckillList;
    }

    public CacheEntry() { }

    public List<Seckill> getSeckillList() {
        return seckillList;
    }

    public void setSeckillList(List<Seckill> seckillList) {
        this.seckillList = seckillList;
    }
}
