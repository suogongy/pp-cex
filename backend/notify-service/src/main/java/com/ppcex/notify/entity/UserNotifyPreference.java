package com.ppcex.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户通知偏好实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_notify_preference")
public class UserNotifyPreference {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型 1-邮件 2-短信 3-站内信 4-推送
     */
    private Integer notifyType;

    /**
     * 业务类型 1-订单 2-交易 3-资产 4-安全 5-系统
     */
    private Integer businessType;

    /**
     * 是否启用 1-启用 0-禁用
     */
    private Integer enabled;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}