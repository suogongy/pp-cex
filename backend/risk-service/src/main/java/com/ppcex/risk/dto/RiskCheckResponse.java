package com.ppcex.risk.dto;

import lombok.Data;

/**
 * 风控检查响应DTO
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class RiskCheckResponse {

    /**
     * 风控检查ID
     */
    private String checkId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 风险评分
     */
    private Integer riskScore;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 风险等级描述
     */
    private String riskLevelDesc;

    /**
     * 检查结果
     */
    private Boolean pass;

    /**
     * 动作类型
     */
    private Integer actionType;

    /**
     * 动作类型描述
     */
    private String actionTypeDesc;

    /**
     * 触发的规则列表
     */
    private java.util.List<TriggeredRule> triggeredRules;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 建议措施
     */
    private String suggestion;

    /**
     * 处理时间戳
     */
    private Long processTimestamp;

    /**
     * 触发的规则信息
     */
    @Data
    public static class TriggeredRule {
        /**
         * 规则ID
         */
        private Long ruleId;

        /**
         * 规则编码
         */
        private String ruleCode;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则类型
         */
        private Integer ruleType;

        /**
         * 风险等级
         */
        private Integer riskLevel;

        /**
         * 触发值
         */
        private java.math.BigDecimal triggerValue;

        /**
         * 阈值
         */
        private java.math.BigDecimal thresholdValue;

        /**
         * 动作类型
         */
        private Integer actionType;
    }
}