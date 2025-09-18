package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风控白名单实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("risk_whitelist")
public class RiskWhitelist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 白名单ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 白名单类型 1-用户 2-IP 3-设备 4-地址
     */
    @TableField("whitelist_type")
    private Integer whitelistType;

    /**
     * 白名单值
     */
    @TableField("whitelist_value")
    private String whitelistValue;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 状态 1-启用 2-禁用
     */
    @TableField("status")
    private Integer status;

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