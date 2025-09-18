package com.ppcex.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应DTO
 */
@Data
public class NotifyResponseDTO {

    /**
     * 通知编号
     */
    private String notifyNo;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息
     */
    private String message;
}