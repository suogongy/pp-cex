package com.ppcex.market.enums;

import lombok.Getter;

@Getter
public enum MarketStatusEnum {
    ACTIVE(1, "正常"),
    PAUSED(2, "暂停"),
    MAINTENANCE(3, "维护");

    private final Integer code;
    private final String description;

    MarketStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MarketStatusEnum getByCode(Integer code) {
        for (MarketStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}