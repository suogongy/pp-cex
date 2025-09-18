package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控规则实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("risk_rule")
public class RiskRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 风控规则ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则类型 1-用户风控 2-交易风控 3-资产风控 4-系统风控
     */
    @TableField("rule_type")
    private Integer ruleType;

    /**
     * 规则分类
     */
    @TableField("rule_category")
    private String ruleCategory;

    /**
     * 风险等级 1-低 2-中 3-高 4-严重
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 状态 1-启用 2-禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 规则表达式(JSON格式)
     */
    @TableField("rule_expression")
    private String ruleExpression;

    /**
     * 阈值
     */
    @TableField("threshold_value")
    private BigDecimal thresholdValue;

    /**
     * 动作类型 1-警告 2-限制 3-冻结 4-拒绝
     */
    @TableField("action_type")
    private Integer actionType;

    /**
     * 动作参数(JSON格式)
     */
    @TableField("action_params")
    private String actionParams;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 是否删除标记（逻辑删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}