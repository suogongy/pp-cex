package com.ppcex.user.service;

import com.ppcex.user.dto.KycInfoResponse;
import com.ppcex.user.dto.KycSubmitRequest;

public interface KycService {

    /**
     * 提交KYC认证
     */
    void submitKyc(Long userId, KycSubmitRequest request);

    /**
     * 获取用户KYC信息
     */
    KycInfoResponse getKycInfo(Long userId);

    /**
     * 审核KYC认证
     */
    void auditKyc(Long kycId, Integer status, String auditUser, String rejectReason);

    /**
     * 重新提交KYC认证
     */
    void resubmitKyc(Long userId, KycSubmitRequest request);

    /**
     * 检查用户是否已提交KYC
     */
    boolean hasSubmittedKyc(Long userId);

    /**
     * 检查用户KYC是否已通过
     */
    boolean isKycApproved(Long userId);

    /**
     * 获取待审核KYC数量
     */
    Long getPendingAuditCount();
}