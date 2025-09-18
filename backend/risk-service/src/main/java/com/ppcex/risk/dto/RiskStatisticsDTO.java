package com.ppcex.risk.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控统计数据DTO
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class RiskStatisticsDTO {

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 总事件数
     */
    private Integer totalEvents;

    /**
     * 高风险事件数
     */
    private Integer highRiskEvents;

    /**
     * 中风险事件数
     */
    private Integer mediumRiskEvents;

    /**
     * 低风险事件数
     */
    private Integer lowRiskEvents;

    /**
     * 已处理事件数
     */
    private Integer processedEvents;

    /**
     * 待处理事件数
     */
    private Integer pendingEvents;

    /**
     * 被阻止用户数
     */
    private Integer blockedUsers;

    /**
     * 被冻结用户数
     */
    private Integer frozenUsers;

    /**
     * 总阻止金额
     */
    private BigDecimal totalBlockedAmount;

    /**
     * 事件处理率
     */
    private BigDecimal processRate;

    /**
     * 高风险事件占比
     */
    private BigDecimal highRiskRate;

    /**
     * 统计时间
     */
    private LocalDateTime statTime;

    /**
     * 趋势数据
     */
    private java.util.List<TrendData> trendData;

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        /**
         * 日期
         */
        private String date;

        /**
         * 事件数量
         */
        private Integer eventCount;

        /**
         * 风险评分
         */
        private Integer riskScore;

        /**
         * 处理率
         */
        private BigDecimal processRate;
    }
}