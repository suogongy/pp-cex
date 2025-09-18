package com.ppcex.risk.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风控事件查询DTO
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class RiskEventQuery {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 页面大小
     */
    private Integer size = 20;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 事件类型
     */
    private Integer eventType;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 动作类型
     */
    private Integer actionType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理人
     */
    private String processor;
}