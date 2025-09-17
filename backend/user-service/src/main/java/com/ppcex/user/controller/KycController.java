package com.ppcex.user.controller;

import com.ppcex.user.dto.ApiResponse;
import com.ppcex.user.dto.KycInfoResponse;
import com.ppcex.user.dto.KycSubmitRequest;
import com.ppcex.user.service.KycService;
import com.ppcex.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ppcex.user.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC认证接口", description = "KYC认证提交、查询等接口")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Slf4j
public class KycController {

    private final KycService kycService;
    private final UserService userService;
    
    @PostMapping("/submit")
    @Operation(summary = "提交KYC认证", description = "提交KYC认证信息")
    public ApiResponse<String> submitKyc(@RequestBody KycSubmitRequest request) {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户ID
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            log.debug("提交KYC认证 - 当前用户ID: {}", userId);

            kycService.submitKyc(userId, request);
            return ApiResponse.success("KYC认证提交成功，请等待审核");
        } catch (Exception e) {
            log.error("提交KYC认证失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/resubmit")
    @Operation(summary = "重新提交KYC认证", description = "重新提交KYC认证信息")
    public ApiResponse<String> resubmitKyc(@RequestBody KycSubmitRequest request) {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("重新提交KYC认证 - 当前用户名: {}", username);

            // 通过用户名获取用户ID
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            kycService.resubmitKyc(userId, request);
            return ApiResponse.success("KYC认证重新提交成功，请等待审核");
        } catch (Exception e) {
            log.error("重新提交KYC认证失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/info")
    @Operation(summary = "获取KYC信息", description = "获取用户KYC认证信息")
    public ApiResponse<KycInfoResponse> getKycInfo() {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("获取KYC信息 - 当前用户名: {}", username);

            // 通过用户名获取用户ID
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            KycInfoResponse kycInfo = kycService.getKycInfo(userId);
            return ApiResponse.success(kycInfo);
        } catch (Exception e) {
            log.error("获取KYC信息失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "获取KYC状态", description = "获取用户KYC认证状态")
    public ApiResponse<KycStatusResponse> getKycStatus() {
        try {
            // 获取当前认证的用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "用户未认证");
            }

            // 从认证信息中获取用户名
            String username = authentication.getName();
            log.debug("获取KYC状态 - 当前用户名: {}", username);

            // 通过用户名获取用户ID
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            boolean hasSubmitted = kycService.hasSubmittedKyc(userId);
            boolean isApproved = kycService.isKycApproved(userId);

            KycStatusResponse response = new KycStatusResponse();
            response.setHasSubmitted(hasSubmitted);
            response.setApproved(isApproved);

            if (hasSubmitted) {
                KycInfoResponse kycInfo = kycService.getKycInfo(userId);
                response.setStatus(kycInfo.getStatus());
                response.setStatusDescription(kycInfo.getStatusDescription());
            } else {
                response.setStatus(-1);
                response.setStatusDescription("未提交");
            }

            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取KYC状态失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 管理员接口
    @PostMapping("/audit/{kycId}")
    @Operation(summary = "审核KYC认证", description = "管理员审核KYC认证")
    public ApiResponse<String> auditKyc(
            @PathVariable Long kycId,
            @RequestBody KycAuditRequest request) {
        try {
            kycService.auditKyc(kycId, request.getStatus(), request.getAuditUser(), request.getRejectReason());
            return ApiResponse.success("KYC认证审核完成");
        } catch (Exception e) {
            log.error("审核KYC认证失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/pending-count")
    @Operation(summary = "获取待审核KYC数量", description = "获取待审核KYC认证数量")
    public ApiResponse<Long> getPendingAuditCount() {
        try {
            Long count = kycService.getPendingAuditCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("获取待审核KYC数量失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // KYC状态响应
    public static class KycStatusResponse {
        private Boolean hasSubmitted;
        private Boolean approved;
        private Integer status;
        private String statusDescription;

        // getters and setters
        public Boolean getHasSubmitted() {
            return hasSubmitted;
        }

        public void setHasSubmitted(Boolean hasSubmitted) {
            this.hasSubmitted = hasSubmitted;
        }

        public Boolean getApproved() {
            return approved;
        }

        public void setApproved(Boolean approved) {
            this.approved = approved;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusDescription() {
            return statusDescription;
        }

        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }
    }

    // KYC审核请求
    public static class KycAuditRequest {
        private Integer status;
        private String auditUser;
        private String rejectReason;

        // getters and setters
        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getAuditUser() {
            return auditUser;
        }

        public void setAuditUser(String auditUser) {
            this.auditUser = auditUser;
        }

        public String getRejectReason() {
            return rejectReason;
        }

        public void setRejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
        }
    }
}