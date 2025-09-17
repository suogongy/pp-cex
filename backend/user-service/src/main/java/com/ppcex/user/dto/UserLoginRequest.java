package com.ppcex.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @Size(min = 6, max = 6, message = "Google验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "Google验证码必须是6位数字")
    @Schema(description = "Google验证码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String googleCode;

    @Size(max = 10, message = "验证码长度不能超过10个字符")
    @Schema(description = "图形验证码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String captcha;
}