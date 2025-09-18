package com.ppcex.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知配置实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notify_config")
public class NotifyConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置类型 1-邮件 2-短信 3-站内信 4-推送 5-Webhook
     */
    private Integer configType;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值(JSON格式)
     */
    private String configValue;

    /**
     * 状态 1-启用 2-禁用
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}