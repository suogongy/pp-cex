package com.ppcex.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "用户登录响应")
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfoResponse userInfo;

    @Schema(description = "是否需要绑定Google 2FA")
    private Boolean requireGoogle2FA;

    @Schema(description = "会话ID")
    private String sessionId;
}