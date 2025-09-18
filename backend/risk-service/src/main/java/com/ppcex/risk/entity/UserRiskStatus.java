package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户风控状态实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_risk_status")
public class UserRiskStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 风险评分(0-100)
     */
    @TableField("risk_score")
    private Integer riskScore;

    /**
     * 风险等级 1-低 2-中 3-高 4-严重
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 状态 1-正常 2-监控 3-限制 4-冻结
     */
    @TableField("status")
    private Integer status;

    /**
     * 冻结原因
     */
    @TableField("freeze_reason")
    private String freezeReason;

    /**
     * 冻结时间
     */
    @TableField("freeze_time")
    private LocalDateTime freezeTime;

    /**
     * 解冻时间
     */
    @TableField("unfreeze_time")
    private LocalDateTime unfreezeTime;

    /**
     * 最后风控时间
     */
    @TableField("last_risk_time")
    private LocalDateTime lastRiskTime;

    /**
     * 风控事件数量
     */
    @TableField("risk_event_count")
    private Integer riskEventCount;

    /**
     * 总登录次数
     */
    @TableField("total_login_count")
    private Integer totalLoginCount;

    /**
     * 失败登录次数
     */
    @TableField("failed_login_count")
    private Integer failedLoginCount;

    /**
     * 可疑IP数量
     */
    @TableField("suspicious_ip_count")
    private Integer suspiciousIpCount;

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