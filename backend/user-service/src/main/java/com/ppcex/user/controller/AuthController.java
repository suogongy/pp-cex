package com.ppcex.user.controller;

import com.ppcex.user.dto.ApiResponse;
import com.ppcex.user.dto.UserLoginRequest;
import com.ppcex.user.dto.UserLoginResponse;
import com.ppcex.user.dto.UserRegisterRequest;
import com.ppcex.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "用户认证接口", description = "用户注册、登录、登出等认证相关接口")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public ApiResponse<String> register(@RequestBody UserRegisterRequest registerRequest) {
        try {
            userService.register(registerRequest);
            return ApiResponse.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录认证")
    public ApiResponse<UserLoginResponse> login(@RequestBody UserLoginRequest loginRequest) {
        try {
            UserLoginResponse response = userService.login(loginRequest);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // 去掉"Bearer "前缀
            userService.logout(jwtToken);
            return ApiResponse.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ApiResponse<String> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ApiResponse.error(400, "刷新令牌不能为空");
            }

            String newToken = userService.refreshToken(refreshToken);
            return ApiResponse.success(newToken);
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否可用")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            return ApiResponse.success(!exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查用户名失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否可用")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.checkEmailExists(email);
            return ApiResponse.success(!exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查邮箱失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否可用")
    public ApiResponse<Boolean> checkPhone(@RequestParam String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            return ApiResponse.success(!exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查手机号失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }
}