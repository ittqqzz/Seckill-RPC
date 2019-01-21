package com.tqz.utils;

import com.tqz.enums.ConstantEnum;

import java.util.Date;

public class DateUtils {

    public static Date addMinutes(Date orderTime, ConstantEnum SuccessKilledTimeout) {
        Date afterDate = new Date(orderTime.getTime() + 60000 * SuccessKilledTimeout.getTimeout());
        return afterDate;
    }
}
