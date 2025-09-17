package com.ppcex.match.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DirectionEnum {
    BUY(1, "买入"),
    SELL(2, "卖出");

    private final Integer code;
    private final String description;
}