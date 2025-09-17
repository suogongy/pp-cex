package com.ppcex.match.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    PENDING(1, "待成交"),
    PARTIALLY_FILLED(2, "部分成交"),
    FULLY_FILLED(3, "完全成交"),
    CANCELLED(4, "已取消"),
    EXPIRED(5, "已过期");

    private final Integer code;
    private final String description;
}