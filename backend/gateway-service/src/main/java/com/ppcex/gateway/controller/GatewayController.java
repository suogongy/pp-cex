package com.ppcex.gateway.controller;

import com.alibaba.fastjson2.JSON;
import com.ppcex.gateway.model.AccessLog;
import com.ppcex.gateway.service.DynamicRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网关控制器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "网关管理", description = "网关服务管理接口")
@RequiredArgsConstructor
public class GatewayController {

    private final DynamicRouteService dynamicRouteService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有路由
     */
    @GetMapping("/routes")
    @Operation(summary = "获取所有路由", description = "获取网关中配置的所有路由信息")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        try {
            List<RouteDefinition> routes = dynamicRouteService.getAllRoutes();

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", routes);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get routes", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "获取路由失败"));
        }
    }

    /**
     * 刷新路由
     */
    @PostMapping("/routes/refresh")
    @Operation(summary = "刷新路由", description = "刷新网关路由配置")
    public ResponseEntity<Map<String, Object>> refreshRoutes() {
        try {
            dynamicRouteService.refreshRoutes();

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "路由刷新成功");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to refresh routes", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "路由刷新失败"));
        }
    }

    /**
     * 查询访问日志
     */
    @GetMapping("/logs")
    @Operation(summary = "查询访问日志", description = "分页查询网关访问日志")
    public ResponseEntity<Map<String, Object>> getAccessLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "请求路径") @RequestParam(required = false) String path) {

        try {
            // 构建查询条件
            List<AccessLog> allLogs = new ArrayList<>();

            // 从Redis中查询日志
            if (startTime != null && endTime != null) {
                allLogs = queryLogsFromRedis(startTime, endTime);
            }

            // 过滤条件
            List<AccessLog> filteredLogs = allLogs.stream()
                .filter(log -> userId == null || userId.equals(log.getUserId()))
                .filter(log -> path == null || log.getPath().contains(path))
                .collect(Collectors.toList());

            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, filteredLogs.size());
            List<AccessLog> pageLogs = filteredLogs.subList(start, end);

            Page<AccessLog> pageResult = new PageImpl<>(pageLogs,
                org.springframework.data.domain.PageRequest.of(page - 1, size, Sort.by("requestTime").descending()),
                filteredLogs.size());

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "list", pageLogs,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", pageResult.getTotalElements(),
                    "pages", pageResult.getTotalPages()
                )
            ));
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get access logs", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "查询访问日志失败"));
        }
    }

    /**
     * 获取访问统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取访问统计", description = "获取网关访问统计信息")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 如果没有指定时间，默认使用今天
            if (startTime == null) {
                startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }

            // 计算总请求数
            long totalRequests = calculateTotalRequests(startTime, endTime);
            statistics.put("totalRequests", totalRequests);

            // 计算成功请求数
            long successRequests = calculateSuccessRequests(startTime, endTime);
            statistics.put("successRequests", successRequests);

            // 计算失败请求数
            long failedRequests = totalRequests - successRequests;
            statistics.put("failedRequests", failedRequests);

            // 计算成功率
            double successRate = totalRequests > 0 ? (double) successRequests / totalRequests * 100 : 0;
            statistics.put("successRate", String.format("%.2f%%", successRate));

            // 计算平均响应时间
            double avgResponseTime = calculateAvgResponseTime(startTime, endTime);
            statistics.put("avgResponseTime", String.format("%.2fms", avgResponseTime));

            // 按服务统计
            Map<String, Object> serviceStats = calculateServiceStatistics(startTime, endTime);
            statistics.put("serviceStatistics", serviceStats);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", statistics);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "获取访问统计失败"));
        }
    }

    /**
     * 获取健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "获取健康状态", description = "获取网关服务健康状态")
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("version", "1.0.0");
            health.put("serviceName", "gateway-service");

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", health);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get health status", e);
            return ResponseEntity.internalServerError().body(buildErrorResponse(500, "获取健康状态失败"));
        }
    }

    /**
     * 从Redis查询日志
     */
    private List<AccessLog> queryLogsFromRedis(LocalDateTime startTime, LocalDateTime endTime) {
        List<AccessLog> logs = new ArrayList<>();

        try {
            // 按日期查询日志
            LocalDateTime current = startTime.toLocalDate().atStartOfDay();
            LocalDateTime end = endTime.toLocalDate().atTime(23, 59, 59);

            while (!current.isAfter(end)) {
                String key = "access:log:" + current.toLocalDate().toString();
                List<Object> dayLogs = redisTemplate.opsForList().range(key, 0, -1);

                if (dayLogs != null) {
                    dayLogs.stream()
                        .filter(obj -> obj instanceof AccessLog)
                        .map(obj -> (AccessLog) obj)
                        .filter(log -> log.getRequestTime().isAfter(startTime) && log.getRequestTime().isBefore(endTime))
                        .forEach(logs::add);
                }

                current = current.plusDays(1);
            }
        } catch (Exception e) {
            log.error("Failed to query logs from Redis", e);
        }

        return logs;
    }

    /**
     * 计算总请求数
     */
    private long calculateTotalRequests(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 这里应该从Redis统计信息中计算
            // 简化实现，返回模拟数据
            return 12580L;
        } catch (Exception e) {
            log.error("Failed to calculate total requests", e);
            return 0L;
        }
    }

    /**
     * 计算成功请求数
     */
    private long calculateSuccessRequests(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 这里应该从Redis统计信息中计算
            // 简化实现，返回模拟数据
            return 12350L;
        } catch (Exception e) {
            log.error("Failed to calculate success requests", e);
            return 0L;
        }
    }

    /**
     * 计算平均响应时间
     */
    private double calculateAvgResponseTime(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 这里应该从Redis统计信息中计算
            // 简化实现，返回模拟数据
            return 125.5;
        } catch (Exception e) {
            log.error("Failed to calculate average response time", e);
            return 0.0;
        }
    }

    /**
     * 计算服务统计
     */
    private Map<String, Object> calculateServiceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> serviceStats = new HashMap<>();

        // 模拟服务统计数据
        Map<String, Object> userServiceStats = new HashMap<>();
        userServiceStats.put("requests", 4500);
        userServiceStats.put("successRate", "99.8%");
        userServiceStats.put("avgResponseTime", "95ms");
        serviceStats.put("user-service", userServiceStats);

        Map<String, Object> tradeServiceStats = new HashMap<>();
        tradeServiceStats.put("requests", 3200);
        tradeServiceStats.put("successRate", "99.5%");
        tradeServiceStats.put("avgResponseTime", "110ms");
        serviceStats.put("trade-service", tradeServiceStats);

        Map<String, Object> walletServiceStats = new HashMap<>();
        walletServiceStats.put("requests", 2800);
        walletServiceStats.put("successRate", "99.9%");
        walletServiceStats.put("avgResponseTime", "85ms");
        serviceStats.put("wallet-service", walletServiceStats);

        return serviceStats;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}