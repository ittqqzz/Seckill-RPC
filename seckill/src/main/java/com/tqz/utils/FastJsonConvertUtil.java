package com.tqz.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tqz.entity.User;

public class FastJsonConvertUtil {

    public static User convertJSONToObject(String message, Object obj){
        User user = JSON.parseObject(message, new TypeReference<User>() {});
        return user;
    }

    public static String convertObjectToJSON(Object obj){
        String text = JSON.toJSONString(obj);
        return text;
    }
}
