package com.ppcex.trade.enums;

import lombok.Getter;

/**
 * 时效类型枚举
 */
@Getter
public enum TimeInForceEnum {

    GTC(1, "撤销前有效"),
    IOC(2, "立即成交或撤销"),
    FOK(3, "全部成交或撤销");

    private final Integer code;
    private final String description;

    TimeInForceEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TimeInForceEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TimeInForceEnum timeInForce : values()) {
            if (timeInForce.getCode().equals(code)) {
                return timeInForce;
            }
        }
        return null;
    }
}