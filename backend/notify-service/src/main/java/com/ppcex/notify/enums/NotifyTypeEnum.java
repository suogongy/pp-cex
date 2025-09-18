package com.ppcex.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NotifyTypeEnum {
    EMAIL(1, "邮件"),
    SMS(2, "短信"),
    IN_APP(3, "站内信"),
    PUSH(4, "推送"),
    WEBHOOK(5, "Webhook");

    private final Integer code;
    private final String description;
}