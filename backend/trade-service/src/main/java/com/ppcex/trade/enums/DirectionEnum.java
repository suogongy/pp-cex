package com.ppcex.trade.enums;

import lombok.Getter;

/**
 * 买卖方向枚举
 */
@Getter
public enum DirectionEnum {

    BUY(1, "买入"),
    SELL(2, "卖出");

    private final Integer code;
    private final String description;

    DirectionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DirectionEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DirectionEnum direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        return null;
    }
}