package com.ppcex.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ppcex.finance.entity.FinancialDailyReport;
import com.ppcex.finance.entity.FinancialFlow;
import com.ppcex.finance.repository.FinancialDailyReportRepository;
import com.ppcex.finance.repository.FinancialFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final FinancialDailyReportRepository financialDailyReportRepository;
    private final FinancialFlowRepository financialFlowRepository;

    @Transactional
    public FinancialDailyReport generateDailyReport(LocalDate reportDate) {
        log.info("生成日报表: date={}", reportDate);

        // 检查是否已存在
        FinancialDailyReport existing = financialDailyReportRepository.getReportByDate(reportDate);
        if (existing != null) {
            log.info("日报表已存在，执行更新操作");
            return updateDailyReport(existing);
        }

        LocalDateTime startTime = reportDate.atStartOfDay();
        LocalDateTime endTime = reportDate.plusDays(1).atStartOfDay();

        FinancialDailyReport report = new FinancialDailyReport();
        report.setReportDate(reportDate);
        report.setReportStatus(FinancialDailyReport.ReportStatus.DRAFT.getCode());

        // 统计各项数据
        report.setTotalTrades(countTrades(startTime, endTime));
        report.setTotalTradeVolume(calculateTradeVolume(startTime, endTime));
        report.setTotalFeeIncome(calculateFeeIncome(startTime, endTime));
        report.setTotalRecharge(calculateBusinessVolume(FinancialFlow.BusinessType.RECHARGE.getCode(), startTime, endTime));
        report.setTotalWithdraw(calculateBusinessVolume(FinancialFlow.BusinessType.WITHDRAW.getCode(), startTime, endTime));
        report.setNetDeposit(report.getTotalRecharge().subtract(report.getTotalWithdraw()));
        report.setRiskEventsCount(countRiskEvents(startTime, endTime));

        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        financialDailyReportRepository.insert(report);
        log.info("日报表生成成功: date={}, trades={}, volume={}, fee={}",
                reportDate, report.getTotalTrades(), report.getTotalTradeVolume(), report.getTotalFeeIncome());

        return report;
    }

    @Transactional
    public FinancialDailyReport updateDailyReport(FinancialDailyReport report) {
        LocalDateTime startTime = report.getReportDate().atStartOfDay();
        LocalDateTime endTime = report.getReportDate().plusDays(1).atStartOfDay();

        // 重新计算所有数据
        report.setTotalTrades(countTrades(startTime, endTime));
        report.setTotalTradeVolume(calculateTradeVolume(startTime, endTime));
        report.setTotalFeeIncome(calculateFeeIncome(startTime, endTime));
        report.setTotalRecharge(calculateBusinessVolume(FinancialFlow.BusinessType.RECHARGE.getCode(), startTime, endTime));
        report.setTotalWithdraw(calculateBusinessVolume(FinancialFlow.BusinessType.WITHDRAW.getCode(), startTime, endTime));
        report.setNetDeposit(report.getTotalRecharge().subtract(report.getTotalWithdraw()));
        report.setRiskEventsCount(countRiskEvents(startTime, endTime));

        report.setUpdateTime(LocalDateTime.now());

        financialDailyReportRepository.updateById(report);
        return report;
    }

    public List<FinancialDailyReport> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return financialDailyReportRepository.getReportsByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalTradeVolume(LocalDate startDate, LocalDate endDate) {
        return financialDailyReportRepository.getTotalTradeVolumeByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalFeeIncome(LocalDate startDate, LocalDate endDate) {
        return financialDailyReportRepository.getTotalFeeIncomeByDateRange(startDate, endDate);
    }

    public BigDecimal getAvgActiveUsers(LocalDate startDate, LocalDate endDate) {
        return financialDailyReportRepository.getAvgActiveUsersByDateRange(startDate, endDate);
    }

    private Long countTrades(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<FinancialFlow> wrapper = new QueryWrapper<>();
        wrapper.in("business_type", FinancialFlow.BusinessType.BUY.getCode(), FinancialFlow.BusinessType.SELL.getCode())
               .between("create_time", startTime, endTime)
               .eq("status", FinancialFlow.Status.SUCCESS.getCode());

        return financialFlowRepository.selectCount(wrapper);
    }

    private BigDecimal calculateTradeVolume(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<FinancialFlow> wrapper = new QueryWrapper<>();
        wrapper.in("business_type", FinancialFlow.BusinessType.BUY.getCode(), FinancialFlow.BusinessType.SELL.getCode())
               .between("create_time", startTime, endTime)
               .eq("status", FinancialFlow.Status.SUCCESS.getCode());

        List<FinancialFlow> flows = financialFlowRepository.selectList(wrapper);
        return flows.stream()
                .map(flow -> flow.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateFeeIncome(LocalDateTime startTime, LocalDateTime endTime) {
        return financialFlowRepository.getTotalFeeByTimeRange(startTime, endTime);
    }

    private BigDecimal calculateBusinessVolume(Integer businessType, LocalDateTime startTime, LocalDateTime endTime) {
        return financialFlowRepository.getTotalAmountByBusinessTypeAndTimeRange(businessType, startTime, endTime);
    }

    private Integer countRiskEvents(LocalDateTime startTime, LocalDateTime endTime) {
        // 这里需要查询风险事件表，简化实现
        return 0;
    }
}