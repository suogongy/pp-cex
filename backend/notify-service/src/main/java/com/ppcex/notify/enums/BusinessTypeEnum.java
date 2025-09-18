package com.ppcex.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum {
    ORDER(1, "订单"),
    TRADE(2, "交易"),
    ASSET(3, "资产"),
    SECURITY(4, "安全"),
    SYSTEM(5, "系统");

    private final Integer code;
    private final String description;
}