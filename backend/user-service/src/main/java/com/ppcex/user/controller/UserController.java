package com.ppcex.user.controller;

import com.ppcex.user.dto.ApiResponse;
import com.ppcex.user.dto.UserInfoResponse;
import com.ppcex.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ApiResponse<UserInfoResponse> getUserInfo() {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.debug("当前认证信息: {}", authentication);

            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("用户未认证");
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("当前用户名: {}", username);

            // 通过用户名获取用户信息
            UserInfoResponse userInfo = userService.getUserInfoByUsername(username);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public ApiResponse<UserInfoResponse> updateUserInfo(@RequestBody UserInfoResponse userInfoRequest) {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("更新用户信息 - 当前用户名: {}", username);

            // 通过用户名获取用户ID
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            UserInfoResponse updatedUserInfo = userService.updateUserInfo(userId, userInfoRequest);
            return ApiResponse.success(updatedUserInfo);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("修改密码 - 当前用户名: {}", username);

            // 通过用户名获取用户ID
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                return ApiResponse.error(404, "用户不存在");
            }

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