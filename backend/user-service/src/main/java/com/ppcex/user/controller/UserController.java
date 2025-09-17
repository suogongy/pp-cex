package com.ppcex.user.controller;

import com.ppcex.user.dto.ApiResponse;
import com.ppcex.user.dto.UserInfoResponse;
import com.ppcex.common.util.JwtUtil;
import com.ppcex.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户信息接口", description = "用户信息查询、更新等接口")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserInfoResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // 去掉"Bearer "前缀
            Long userId = JwtUtil.getClaimFromToken(jwtToken, "userId");

            UserInfoResponse userInfo = userService.getUserInfo(userId);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public ApiResponse<UserInfoResponse> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserInfoResponse userInfoRequest) {
        try {
            String jwtToken = token.substring(7); // 去掉"Bearer "前缀
            Long userId = JwtUtil.getClaimFromToken(jwtToken, "userId");

            UserInfoResponse updatedUserInfo = userService.updateUserInfo(userId, userInfoRequest);
            return ApiResponse.success(updatedUserInfo);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public ApiResponse<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request) {
        try {
            String jwtToken = token.substring(7); // 去掉"Bearer "前缀
            Long userId = JwtUtil.getClaimFromToken(jwtToken, "userId");

            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 修改密码请求DTO
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}