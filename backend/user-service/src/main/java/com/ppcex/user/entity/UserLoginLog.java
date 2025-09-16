package com.ppcex.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户登录日志表")
@TableName("user_login_log")
public class UserLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "登录类型 1-密码 2-Google 3-SMS")
    @TableField("login_type")
    private Integer loginType;

    @Schema(description = "登录时间")
    @TableField("login_time")
    private LocalDateTime loginTime;

    @Schema(description = "登录IP")
    @TableField("login_ip")
    private String loginIp;

    @Schema(description = "登录地点")
    @TableField("login_location")
    private String loginLocation;

    @Schema(description = "设备信息")
    @TableField("device_info")
    private String deviceInfo;

    @Schema(description = "用户代理")
    @TableField("user_agent")
    private String userAgent;

    @Schema(description = "登录结果 1-成功 2-失败")
    @TableField("login_result")
    private Integer loginResult;

    @Schema(description = "失败原因")
    @TableField("fail_reason")
    private String failReason;

    @Schema(description = "会话ID")
    @TableField("session_id")
    private String sessionId;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "逻辑删除标记 0-未删除 1-已删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}