package com.ppcex.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息响应")
public class UserInfoResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户编号")
    private String userNo;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "时区")
    private String timezone;

    @Schema(description = "状态 1-正常 2-冻结 3-注销")
    private Integer status;

    @Schema(description = "KYC状态 0-未认证 1-已认证")
    private Integer kycStatus;

    @Schema(description = "Google认证启用")
    private Integer googleAuthEnabled;

    @Schema(description = "注册时间")
    private LocalDateTime registerTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}