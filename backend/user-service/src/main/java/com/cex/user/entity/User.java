package com.cex.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Accessors(chain = true)
@TableName("user_info")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    @TableField("user_no")
    private String userNo;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 密码盐值
     */
    @TableField("salt")
    private String salt;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态 1-正常 2-冻结 3-注销
     */
    @TableField("status")
    private Integer status;

    /**
     * KYC状态 0-未认证 1-已认证 2-审核中 3-已拒绝
     */
    @TableField("kyc_status")
    private Integer kycStatus;

    /**
     * 用户类型 1-普通用户 2-VIP用户 3-机构用户
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 邀请码
     */
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 被邀请人ID
     */
    @TableField("inviter_id")
    private Long inviterId;

    /**
     * 注册时间
     */
    @TableField("register_time")
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount;

    /**
     * 锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * Google认证密钥
     */
    @TableField("google_auth_secret")
    private String googleAuthSecret;

    /**
     * Google认证启用状态
     */
    @TableField("google_auth_enabled")
    private Boolean googleAuthEnabled;

    /**
     * 邮箱验证状态
     */
    @TableField("email_verified")
    private Boolean emailVerified;

    /**
     * 手机验证状态
     */
    @TableField("phone_verified")
    private Boolean phoneVerified;

    /**
     * 是否启用API
     */
    @TableField("api_enabled")
    private Boolean apiEnabled;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * API密钥密钥
     */
    @TableField("api_secret")
    private String apiSecret;

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
     * 逻辑删除标记
     */
    @TableField("deleted")
    private Integer deleted;

    /**
     * 版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return status != null && status == 1;
    }

    /**
     * 是否已冻结
     */
    public boolean isFrozen() {
        return status != null && status == 2;
    }

    /**
     * 是否已注销
     */
    public boolean isDeleted() {
        return status != null && status == 3;
    }

    /**
     * 是否已KYC认证
     */
    public boolean isKycVerified() {
        return kycStatus != null && kycStatus == 1;
    }

    /**
     * 是否KYC审核中
     */
    public boolean isKycPending() {
        return kycStatus != null && kycStatus == 2;
    }

    /**
     * 是否KYC已拒绝
     */
    public boolean isKycRejected() {
        return kycStatus != null && kycStatus == 3;
    }

    /**
     * 是否启用Google认证
     */
    public boolean isGoogleAuthEnabled() {
        return googleAuthEnabled != null && googleAuthEnabled;
    }

    /**
     * 是否邮箱已验证
     */
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }

    /**
     * 是否手机已验证
     */
    public boolean isPhoneVerified() {
        return phoneVerified != null && phoneVerified;
    }

    /**
     * 是否启用API
     */
    public boolean isApiEnabled() {
        return apiEnabled != null && apiEnabled;
    }

    /**
     * 是否被锁定
     */
    public boolean isLocked() {
        return lockTime != null && lockTime.isAfter(LocalDateTime.now());
    }

    /**
     * 检查密码重试次数
     */
    public boolean isPasswordRetryExceeded(int maxRetry) {
        return loginFailCount != null && loginFailCount >= maxRetry;
    }
}