package com.tqz.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = -6140012527131929480L;
    private String phone;
    private Long seckillId;

    public User(String phone, Long seckillId) {
        this.phone = phone;
        this.seckillId = seckillId;
    }

    public User() {
    }
}
