package com.ppcex.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通知记录实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notify_record")
public class NotifyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知编号
     */
    private String notifyNo;

    /**
     * 业务类型 1-订单 2-交易 3-资产 4-安全 5-系统
     */
    private Integer businessType;

    /**
     * 通知类型 1-邮件 2-短信 3-站内信 4-推送
     */
    private Integer notifyType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 接收者
     */
    private String recipient;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板变量(JSON格式)
     */
    private String templateVars;

    /**
     * 状态 1-待发送 2-发送中 3-已发送 4-发送失败
     */
    private Integer status;

    /**
     * 发送次数
     */
    private Integer sendCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}