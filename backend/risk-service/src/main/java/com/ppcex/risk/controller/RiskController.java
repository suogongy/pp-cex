package com.ppcex.risk.controller;

import com.ppcex.common.core.result.Result;
import com.ppcex.common.core.web.BaseController;
import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.dto.RiskCheckResponse;
import com.ppcex.risk.dto.RiskEventQuery;
import com.ppcex.risk.dto.RiskStatisticsDTO;
import com.ppcex.risk.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风控控制器
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "风控管理", description = "风控相关接口")
@RequiredArgsConstructor
public class RiskController extends BaseController {

    private final RiskService riskService;

    /**
     * 实时风控检查
     */
    @PostMapping("/check")
    @Operation(summary = "实时风控检查", description = "对用户行为进行实时风控检查")
    public Result<RiskCheckResponse> checkRisk(@Validated @RequestBody RiskCheckRequest request) {
        try {
            log.info("风控检查请求，用户ID: {}, 事件类型: {}", request.getUserId(), request.getEventType());
            RiskCheckResponse response = riskService.checkRisk(request);
            log.info("风控检查完成，用户ID: {}, 风险评分: {}, 是否通过: {}",
                    request.getUserId(), response.getRiskScore(), response.getPass());
            return Result.success(response);
        } catch (Exception e) {
            log.error("风控检查失败", e);
            return Result.fail("风控检查失败: " + e.getMessage());
        }
    }

    /**
     * 批量风控检查
     */
    @PostMapping("/batch-check")
    @Operation(summary = "批量风控检查", description = "批量对用户行为进行风控检查")
    public Result<Map<String, RiskCheckResponse>> batchCheckRisk(@RequestBody Map<String, RiskCheckRequest> requests) {
        try {
            log.info("批量风控检查请求，请求数量: {}", requests.size());
            Map<String, RiskCheckResponse> responses = riskService.batchCheckRisk(requests);
            log.info("批量风控检查完成，响应数量: {}", responses.size());
            return Result.success(responses);
        } catch (Exception e) {
            log.error("批量风控检查失败", e);
            return Result.fail("批量风控检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户风险评分
     */
    @GetMapping("/user/{userId}/score")
    @Operation(summary = "获取用户风险评分", description = "获取指定用户的风险评分")
    public Result<Integer> getUserRiskScore(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            int riskScore = riskService.calculateUserRiskScore(userId, null);
            return Result.success(riskScore);
        } catch (Exception e) {
            log.error("获取用户风险评分失败，用户ID: {}", userId, e);
            return Result.fail("获取用户风险评分失败: " + e.getMessage());
        }
    }

    /**
     * 查询风控事件
     */
    @GetMapping("/events")
    @Operation(summary = "查询风控事件", description = "分页查询风控事件列表")
    public Result<Object> queryRiskEvents(RiskEventQuery query) {
        try {
            Object result = riskService.queryRiskEvents(query);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询风控事件失败", e);
            return Result.fail("查询风控事件失败: " + e.getMessage());
        }
    }

    /**
     * 处理风控事件
     */
    @PostMapping("/events/{eventId}/process")
    @Operation(summary = "处理风控事件", description = "处理指定的风控事件")
    public Result<Boolean> processRiskEvent(
            @Parameter(description = "事件ID") @PathVariable Long eventId,
            @RequestParam String processor,
            @RequestParam(required = false) String remark) {
        try {
            boolean result = riskService.processRiskEvent(eventId, processor, remark);
            return Result.success(result);
        } catch (Exception e) {
            log.error("处理风控事件失败，事件ID: {}", eventId, e);
            return Result.fail("处理风控事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取风控统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取风控统计", description = "获取风控相关统计数据")
    public Result<RiskStatisticsDTO> getRiskStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            RiskStatisticsDTO statistics = riskService.getRiskStatistics(startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取风控统计失败", e);
            return Result.fail("获取风控统计失败: " + e.getMessage());
        }
    }

    /**
     * 检查白名单
     */
    @GetMapping("/whitelist/check")
    @Operation(summary = "检查白名单", description = "检查指定值是否在白名单中")
    public Result<Boolean> checkWhitelist(
            @RequestParam Integer type,
            @RequestParam String value) {
        try {
            boolean result = riskService.checkWhitelist(type, value);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查白名单失败", e);
            return Result.fail("检查白名单失败: " + e.getMessage());
        }
    }

    /**
     * 记录用户行为
     */
    @PostMapping("/behavior/log")
    @Operation(summary = "记录用户行为", description = "记录用户行为日志")
    public Result<Void> logUserBehavior(
            @RequestParam Long userId,
            @RequestParam Integer behaviorType,
            @RequestParam String action,
            @RequestBody Map<String, Object> context) {
        try {
            riskService.logUserBehavior(userId, behaviorType, action, context);
            return Result.success();
        } catch (Exception e) {
            log.error("记录用户行为失败", e);
            return Result.fail("记录用户行为失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "风控服务健康检查")
    public Result<String> health() {
        return Result.success("risk-service is running");
    }
}