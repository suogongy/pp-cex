package com.cex.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {

    /**
     * 用户名/邮箱/手机号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * Google验证码
     */
    private String googleCode;

    /**
     * 短信验证码
     */
    private String smsCode;

    /**
     * 邮箱验证码
     */
    private String emailCode;

    /**
     * 图片验证码
     */
    private String captcha;

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 登录类型 1-密码登录 2-验证码登录
     */
    private String loginType = "1";

    /**
     * 是否记住我
     */
    private Boolean rememberMe = false;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 来源 1-Web 2-App 3-API
     */
    private String source = "1";

    /**
     * 验证是否需要二次验证
     */
    public boolean needSecondFactor() {
        return googleCode != null || smsCode != null || emailCode != null;
    }

    /**
     * 验证登录类型
     */
    public boolean isPasswordLogin() {
        return "1".equals(loginType);
    }

    /**
     * 验证登录类型
     */
    public boolean isVerificationCodeLogin() {
        return "2".equals(loginType);
    }
}