package com.ppcex.risk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为记录实体类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_behavior_log")
public class UserBehaviorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行为ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 行为类型 1-登录 2-交易 3-充值 4-提现 5-查看 6-其他
     */
    @TableField("behavior_type")
    private Integer behaviorType;

    /**
     * 操作动作
     */
    @TableField("action")
    private String action;

    /**
     * 设备信息(JSON格式)
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 地理位置
     */
    @TableField("location")
    private String location;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应状态
     */
    @TableField("response_status")
    private Integer responseStatus;

    /**
     * 响应时间(ms)
     */
    @TableField("response_time")
    private Integer responseTime;

    /**
     * 风险评分
     */
    @TableField("risk_score")
    private Integer riskScore;

    /**
     * 是否可疑 0-正常 1-可疑
     */
    @TableField("is_suspicious")
    private Integer isSuspicious;

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
     * 是否删除标记（逻辑删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}