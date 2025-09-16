package com.ppcex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ppcex.user.dto.KycInfoResponse;
import com.ppcex.user.dto.KycSubmitRequest;
import com.ppcex.user.entity.UserInfo;
import com.ppcex.user.entity.UserKyc;
import com.ppcex.user.repository.UserInfoRepository;
import com.ppcex.user.repository.UserKycRepository;
import com.ppcex.user.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final UserKycRepository userKycRepository;
    private final UserInfoRepository userInfoRepository;

    @Override
    @Transactional
    public void submitKyc(Long userId, KycSubmitRequest request) {
        log.info("用户提交KYC认证开始: {}", userId);

        // 检查用户是否存在
        UserInfo userInfo = userInfoRepository.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否已提交KYC
        if (hasSubmittedKyc(userId)) {
            UserKyc existingKyc = userKycRepository.selectByUserId(userId);
            if (existingKyc.getStatus() == 0) {
                throw new RuntimeException("KYC认证正在审核中，请勿重复提交");
            } else if (existingKyc.getStatus() == 1) {
                throw new RuntimeException("KYC认证已通过，无需重新提交");
            }
        }

        // 创建KYC记录
        UserKyc userKyc = new UserKyc();
        userKyc.setUserId(userId);
        userKyc.setRealName(request.getRealName());
        userKyc.setIdCardType(request.getIdCardType());
        userKyc.setIdCardNo(request.getIdCardNo());
        userKyc.setIdCardFront(request.getIdCardFront());
        userKyc.setIdCardBack(request.getIdCardBack());
        userKyc.setIdCardHand(request.getIdCardHand());
        userKyc.setNationality(request.getNationality());
        userKyc.setBirthday(request.getBirthday());
        userKyc.setGender(request.getGender());
        userKyc.setAddress(request.getAddress());
        userKyc.setOccupation(request.getOccupation());
        userKyc.setPurpose(request.getPurpose());
        userKyc.setStatus(0); // 待审核

        // 保存KYC记录
        userKycRepository.insert(userKyc);

        log.info("用户提交KYC认证成功: {}", userId);
    }

    @Override
    public KycInfoResponse getKycInfo(Long userId) {
        UserKyc userKyc = userKycRepository.selectByUserId(userId);
        if (userKyc == null) {
            throw new RuntimeException("用户未提交KYC认证");
        }

        KycInfoResponse response = new KycInfoResponse();
        BeanUtils.copyProperties(userKyc, response);

        // 设置状态描述
        response.setStatusDescription(response.getStatusDescription());

        return response;
    }

    @Override
    @Transactional
    public void auditKyc(Long kycId, Integer status, String auditUser, String rejectReason) {
        log.info("审核KYC认证开始: {}, 状态: {}", kycId, status);

        UserKyc userKyc = userKycRepository.selectById(kycId);
        if (userKyc == null) {
            throw new RuntimeException("KYC记录不存在");
        }

        if (userKyc.getStatus() != 0) {
            throw new RuntimeException("KYC认证已审核完成");
        }

        // 更新KYC状态
        userKycRepository.updateAuditStatus(
            userKyc.getUserId(),
            status,
            auditUser,
            rejectReason,
            LocalDateTime.now()
        );

        // 如果审核通过，更新用户KYC状态
        if (status == 1) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userKyc.getUserId());
            userInfo.setKycStatus(1); // 已认证
            userInfoRepository.updateById(userInfo);
        }

        log.info("审核KYC认证完成: {}, 状态: {}", kycId, status);
    }

    @Override
    @Transactional
    public void resubmitKyc(Long userId, KycSubmitRequest request) {
        log.info("用户重新提交KYC认证开始: {}", userId);

        // 检查用户是否存在
        UserInfo userInfo = userInfoRepository.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否有KYC记录
        UserKyc existingKyc = userKycRepository.selectByUserId(userId);
        if (existingKyc == null) {
            throw new RuntimeException("请先提交KYC认证");
        }

        if (existingKyc.getStatus() == 0) {
            throw new RuntimeException("KYC认证正在审核中，请勿重复提交");
        } else if (existingKyc.getStatus() == 1) {
            throw new RuntimeException("KYC认证已通过，无需重新提交");
        }

        // 更新KYC信息
        existingKyc.setRealName(request.getRealName());
        existingKyc.setIdCardType(request.getIdCardType());
        existingKyc.setIdCardNo(request.getIdCardNo());
        existingKyc.setIdCardFront(request.getIdCardFront());
        existingKyc.setIdCardBack(request.getIdCardBack());
        existingKyc.setIdCardHand(request.getIdCardHand());
        existingKyc.setNationality(request.getNationality());
        existingKyc.setBirthday(request.getBirthday());
        existingKyc.setGender(request.getGender());
        existingKyc.setAddress(request.getAddress());
        existingKyc.setOccupation(request.getOccupation());
        existingKyc.setPurpose(request.getPurpose());
        existingKyc.setStatus(0); // 重新设置为待审核
        existingKyc.setRejectReason(null); // 清空拒绝原因

        // 更新KYC记录
        userKycRepository.updateById(existingKyc);

        log.info("用户重新提交KYC认证成功: {}", userId);
    }

    @Override
    public boolean hasSubmittedKyc(Long userId) {
        return userKycRepository.selectByUserId(userId) != null;
    }

    @Override
    public boolean isKycApproved(Long userId) {
        UserKyc userKyc = userKycRepository.selectByUserId(userId);
        return userKyc != null && userKyc.getStatus() == 1;
    }

    @Override
    public Long getPendingAuditCount() {
        return userKycRepository.selectPendingAuditCount();
    }
}