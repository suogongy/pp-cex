package com.ppcex.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知模板实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notify_template")
public class NotifyTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型 1-邮件 2-短信 3-站内信 4-推送
     */
    private Integer templateType;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 模板变量(JSON格式)
     */
    private String templateVars;

    /**
     * 语言
     */
    private String language;

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