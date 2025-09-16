package com.ppcex.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.finance.dto.FinancialFlowDTO;
import com.ppcex.finance.dto.RiskCheckRequest;
import com.ppcex.finance.dto.RiskCheckResult;
import com.ppcex.finance.entity.FinancialDailyReport;
import com.ppcex.finance.entity.FinancialFlow;
import com.ppcex.finance.service.FinancialFlowService;
import com.ppcex.finance.service.FinancialReportService;
import com.ppcex.finance.service.RiskControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialFlowService financialFlowService;
    private final FinancialReportService financialReportService;
    private final RiskControlService riskControlService;

    @PostMapping("/flows")
    public ResponseEntity<FinancialFlow> createFinancialFlow(@Valid @RequestBody FinancialFlowDTO dto) {
        FinancialFlow flow = financialFlowService.createFinancialFlow(dto);
        return ResponseEntity.ok(flow);
    }

    @GetMapping("/flows/user/{userId}")
    public ResponseEntity<Page<FinancialFlow>> getUserFlows(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FinancialFlow> flows = financialFlowService.getUserFlows(userId, page, size);
        return ResponseEntity.ok(flows);
    }

    @GetMapping("/flows/user/{userId}/recent")
    public ResponseEntity<List<FinancialFlow>> getUserRecentFlows(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<FinancialFlow> flows = financialFlowService.getUserRecentFlows(userId, limit);
        return ResponseEntity.ok(flows);
    }

    @GetMapping("/assets/user/{userId}/coin/{coinId}")
    public ResponseEntity<BigDecimal> getUserAsset(
            @PathVariable Long userId,
            @PathVariable String coinId) {
        BigDecimal asset = financialFlowService.getUserTotalAsset(userId, coinId);
        return ResponseEntity.ok(asset);
    }

    @GetMapping("/stats/fee-income")
    public ResponseEntity<BigDecimal> getFeeIncome(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime endTime) {
        BigDecimal feeIncome = financialFlowService.getTotalFeeIncome(startTime, endTime);
        return ResponseEntity.ok(feeIncome);
    }

    @GetMapping("/stats/volume")
    public ResponseEntity<BigDecimal> getBusinessVolume(
            @RequestParam Integer businessType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime endTime) {
        BigDecimal volume = financialFlowService.getBusinessVolume(businessType, startTime, endTime);
        return ResponseEntity.ok(volume);
    }

    @PostMapping("/reports/daily")
    public ResponseEntity<FinancialDailyReport> generateDailyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reportDate) {
        FinancialDailyReport report = financialReportService.generateDailyReport(reportDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<List<FinancialDailyReport>> getDailyReports(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<FinancialDailyReport> reports = financialReportService.getReportsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reports/stats")
    public ResponseEntity<com.ppcex.finance.dto.FinancialStatsDTO> getFinancialStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        com.ppcex.finance.dto.FinancialStatsDTO stats = new com.ppcex.finance.dto.FinancialStatsDTO();
        stats.setTotalTradeVolume(financialReportService.getTotalTradeVolume(startDate, endDate));
        stats.setTotalFeeIncome(financialReportService.getTotalFeeIncome(startDate, endDate));
        stats.setAvgActiveUsers(financialReportService.getAvgActiveUsers(startDate, endDate));
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/risk/check")
    public ResponseEntity<RiskCheckResult> checkRisk(@Valid @RequestBody RiskCheckRequest request) {
        RiskCheckResult result = riskControlService.checkRisk(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/risk/ip-blacklist/add")
    public ResponseEntity<String> addToIPBlacklist(
            @RequestParam String ip,
            @RequestParam String reason) {
        riskControlService.addToIPBlacklist(ip, reason);
        return ResponseEntity.ok("IP已加入黑名单");
    }

    @PostMapping("/risk/ip-blacklist/remove")
    public ResponseEntity<String> removeFromIPBlacklist(@RequestParam String ip) {
        riskControlService.removeFromIPBlacklist(ip);
        return ResponseEntity.ok("IP已移出黑名单");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Finance Service is running");
    }
}