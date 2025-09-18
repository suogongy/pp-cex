package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控事件实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("risk_event")
public class RiskEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 风控事件ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 事件编号
     */
    @TableField("event_no")
    private String eventNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 规则ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 事件类型 1-登录 2-交易 3-充值 4-提现 5-注册 6-其他
     */
    @TableField("event_type")
    private Integer eventType;

    /**
     * 风险等级 1-低 2-中 3-高 4-严重
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 事件数据(JSON格式)
     */
    @TableField("event_data")
    private String eventData;

    /**
     * 触发值
     */
    @TableField("trigger_value")
    private BigDecimal triggerValue;

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
     * 动作结果
     */
    @TableField("action_result")
    private String actionResult;

    /**
     * 状态 1-待处理 2-处理中 3-已处理 4-已忽略
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理时间
     */
    @TableField("processing_time")
    private LocalDateTime processingTime;

    /**
     * 处理人
     */
    @TableField("processor")
    private String processor;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
     * 是否删除标记（逻辑删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}