package com.ppcex.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知状态枚举
 */
@Getter
@AllArgsConstructor
public enum NotifyStatusEnum {
    PENDING(1, "待发送"),
    SENDING(2, "发送中"),
    SENT(3, "已发送"),
    FAILED(4, "发送失败");

    private final Integer code;
    private final String description;
}