package com.tqz.enums;

import java.io.Serializable;

public enum ConstantEnum implements Serializable {
    SUCCESS_KILLED_SENDING("0"),      //发送中
    SUCCESS_KILLED_SEND_SUCCESS("1"), //成功
    SUCCESS_KILLED_SEND_FAILURE("2"), //失败
    SUCCESS_KILLED_TIMEOUT(1)         //分钟超时单位：min
    ;

    ConstantEnum(String status) {
        this.status = status;
    }

    ConstantEnum(Integer timeout) {
        this.timeout = timeout;
    }

    private String status;
    private Integer timeout;

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
