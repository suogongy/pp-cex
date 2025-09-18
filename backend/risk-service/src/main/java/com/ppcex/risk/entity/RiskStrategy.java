package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风控策略实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("risk_strategy")
public class RiskStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 策略编码
     */
    @TableField("strategy_code")
    private String strategyCode;

    /**
     * 策略名称
     */
    @TableField("strategy_name")
    private String strategyName;

    /**
     * 策略类型 1-用户策略 2-交易策略 3-资产策略
     */
    @TableField("strategy_type")
    private Integer strategyType;

    /**
     * 状态 1-启用 2-禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 优先级(数字越小优先级越高)
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 规则ID列表(JSON数组)
     */
    @TableField("rule_ids")
    private String ruleIds;

    /**
     * 匹配类型 1-任意匹配 2-全部匹配 3-加权匹配
     */
    @TableField("match_type")
    private Integer matchType;

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
     * 策略描述
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