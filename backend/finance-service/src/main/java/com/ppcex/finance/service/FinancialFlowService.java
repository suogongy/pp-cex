package com.ppcex.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.finance.dto.FinancialFlowDTO;
import com.ppcex.finance.entity.FinancialFlow;
import com.ppcex.finance.repository.FinancialFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialFlowService {

    private final FinancialFlowRepository financialFlowRepository;

    @Transactional
    public FinancialFlow createFinancialFlow(FinancialFlowDTO dto) {
        log.info("创建资金流水: userId={}, coinId={}, amount={}, businessType={}",
                dto.getUserId(), dto.getCoinId(), dto.getAmount(), dto.getBusinessType());

        // 验证余额变动
        validateBalanceChange(dto);

        FinancialFlow flow = new FinancialFlow();
        BeanUtils.copyProperties(dto, flow);

        // 生成流水编号
        if (flow.getFlowNo() == null) {
            flow.setFlowNo(generateFlowNo());
        }

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        flow.setCreateTime(now);
        flow.setUpdateTime(now);

        financialFlowRepository.insert(flow);

        log.info("资金流水创建成功: flowNo={}", flow.getFlowNo());
        return flow;
    }

    @Transactional
    public FinancialFlow recordFlow(Long userId, String coinId, String coinName,
                                   Integer businessType, BigDecimal amount,
                                   BigDecimal balanceBefore, BigDecimal balanceAfter,
                                   BigDecimal fee, String remark, String refOrderNo) {

        FinancialFlowDTO dto = new FinancialFlowDTO()
                .setUserId(userId)
                .setCoinId(coinId)
                .setCoinName(coinName)
                .setBusinessType(businessType)
                .setAmount(amount)
                .setBalanceBefore(balanceBefore)
                .setBalanceAfter(balanceAfter)
                .setFee(fee)
                .setStatus(FinancialFlow.Status.SUCCESS.getCode())
                .setRemark(remark)
                .setRefOrderNo(refOrderNo);

        return createFinancialFlow(dto);
    }

    public Page<FinancialFlow> getUserFlows(Long userId, int page, int size) {
        QueryWrapper<FinancialFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");

        return financialFlowRepository.selectPage(new Page<>(page, size), wrapper);
    }

    public List<FinancialFlow> getUserRecentFlows(Long userId, int limit) {
        return financialFlowRepository.getUserRecentFlows(userId, limit);
    }

    public BigDecimal getUserTotalAsset(Long userId, String coinId) {
        return financialFlowRepository.getTotalAmountByUserAndCoin(userId, coinId);
    }

    public BigDecimal getTotalFeeIncome(LocalDateTime startTime, LocalDateTime endTime) {
        return financialFlowRepository.getTotalFeeByTimeRange(startTime, endTime);
    }

    public BigDecimal getBusinessVolume(Integer businessType, LocalDateTime startTime, LocalDateTime endTime) {
        return financialFlowRepository.getTotalAmountByBusinessTypeAndTimeRange(businessType, startTime, endTime);
    }

    private void validateBalanceChange(FinancialFlowDTO dto) {
        // 验证余额变动的合理性
        BigDecimal expectedBalance = dto.getBalanceBefore().add(dto.getAmount()).subtract(dto.getFee());

        if (expectedBalance.compareTo(dto.getBalanceAfter()) != 0) {
            throw new IllegalArgumentException("余额变动计算错误: 期望=" + expectedBalance + ", 实际=" + dto.getBalanceAfter());
        }
    }

    private String generateFlowNo() {
        return "FLW" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}