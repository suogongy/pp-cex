package com.ppcex.match.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderTypeEnum {
    LIMIT(1, "限价单"),
    MARKET(2, "市价单");

    private final Integer code;
    private final String description;
}