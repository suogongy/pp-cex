package com.ppcex.trade.enums;

import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
public enum OrderTypeEnum {

    LIMIT(1, "限价单"),
    MARKET(2, "市价单");

    private final Integer code;
    private final String description;

    OrderTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderTypeEnum orderType : values()) {
            if (orderType.getCode().equals(code)) {
                return orderType;
            }
        }
        return null;
    }
}