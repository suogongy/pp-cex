package com.ppcex.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户基本信息表")
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户编号")
    @TableField("user_no")
    private String userNo;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "密码哈希")
    @TableField("password_hash")
    private String passwordHash;

    @Schema(description = "密码盐值")
    @TableField("salt")
    private String salt;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "国家")
    @TableField("country")
    private String country;

    @Schema(description = "语言")
    @TableField("language")
    private String language;

    @Schema(description = "时区")
    @TableField("timezone")
    private String timezone;

    @Schema(description = "状态 1-正常 2-冻结 3-注销")
    @TableField("status")
    private Integer status;

    @Schema(description = "KYC状态 0-未认证 1-已认证")
    @TableField("kyc_status")
    private Integer kycStatus;

    @Schema(description = "注册时间")
    @TableField("register_time")
    private LocalDateTime registerTime;

    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    @Schema(description = "Google认证密钥")
    @TableField("google_auth_secret")
    private String googleAuthSecret;

    @Schema(description = "Google认证启用")
    @TableField("google_auth_enabled")
    private Integer googleAuthEnabled;

    @Schema(description = "登录失败次数")
    @TableField("login_failed_count")
    private Integer loginFailedCount;

    @Schema(description = "账户锁定到期时间")
    @TableField("account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Schema(description = "邀请码")
    @TableField("invite_code")
    private String inviteCode;

    @Schema(description = "邀请人ID")
    @TableField("inviter_id")
    private Long inviterId;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private String createBy;

    @Schema(description = "更新人")
    @TableField("update_by")
    private String updateBy;

    @Schema(description = "逻辑删除标记 0-未删除 1-已删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}