package com.ppcex.market.enums;

import lombok.Getter;

@Getter
public enum KlineIntervalEnum {
    ONE_MINUTE("1m", "1分钟", 60),
    FIVE_MINUTES("5m", "5分钟", 300),
    FIFTEEN_MINUTES("15m", "15分钟", 900),
    THIRTY_MINUTES("30m", "30分钟", 1800),
    ONE_HOUR("1h", "1小时", 3600),
    FOUR_HOURS("4h", "4小时", 14400),
    ONE_DAY("1d", "1天", 86400),
    ONE_WEEK("1w", "1周", 604800),
    ONE_MONTH("1M", "1月", 2592000);

    private final String code;
    private final String description;
    private final int seconds;

    KlineIntervalEnum(String code, String description, int seconds) {
        this.code = code;
        this.description = description;
        this.seconds = seconds;
    }

    public static KlineIntervalEnum getByCode(String code) {
        for (KlineIntervalEnum interval : values()) {
            if (interval.getCode().equals(code)) {
                return interval;
            }
        }
        return null;
    }
}