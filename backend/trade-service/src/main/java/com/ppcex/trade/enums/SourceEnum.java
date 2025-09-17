package com.ppcex.trade.enums;

import lombok.Getter;

/**
 * 订单来源枚举
 */
@Getter
public enum SourceEnum {

    WEB(1, "网页端"),
    API(2, "API接口"),
    APP(3, "移动端");

    private final Integer code;
    private final String description;

    SourceEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SourceEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SourceEnum source : values()) {
            if (source.getCode().equals(code)) {
                return source;
            }
        }
        return null;
    }
}