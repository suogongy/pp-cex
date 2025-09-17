package com.ppcex.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

public class JsonUtil {

    public static String toJsonString(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        return JSON.parseObject(text, typeReference);
    }
}