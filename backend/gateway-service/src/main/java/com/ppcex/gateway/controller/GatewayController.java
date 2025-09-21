package com.ppcex.gateway.controller;

import com.ppcex.gateway.service.DynamicRouteService;
import com.ppcex.gateway.service.HealthCheckService;
import com.ppcex.gateway.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "网关管理", description = "网关服务管理接口")
@RequiredArgsConstructor
@Slf4j
public class GatewayController {

    private final DynamicRouteService dynamicRouteService;
    private final HealthCheckService healthCheckService;
    private final JwtTokenService jwtTokenService;

    @GetMapping("/routes")
    @Operation(summary = "获取所有路由", description = "获取网关中配置的所有路由信息")
    public Flux<RouteDefinition> getAllRoutes() {
        return dynamicRouteService.getAllRoutes();
    }

    @GetMapping("/routes/{routeId}")
    @Operation(summary = "获取指定路由", description = "根据路由ID获取路由详细信息")
    public Mono<ResponseEntity<RouteDefinition>> getRouteById(
            @Parameter(description = "路由ID") @PathVariable String routeId) {
        return dynamicRouteService.getRouteById(routeId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/routes")
    @Operation(summary = "添加路由", description = "向网关添加新的路由配置")
    public Mono<ResponseEntity<String>> addRoute(@RequestBody RouteDefinition routeDefinition) {
        return dynamicRouteService.addRoute(routeDefinition)
                .thenReturn(ResponseEntity.ok("Route added successfully"))
                .onErrorReturn(ResponseEntity.badRequest().body("Failed to add route"));
    }

    @PutMapping("/routes/{routeId}")
    @Operation(summary = "更新路由", description = "更新指定的路由配置")
    public Mono<ResponseEntity<String>> updateRoute(
            @Parameter(description = "路由ID") @PathVariable String routeId,
            @RequestBody RouteDefinition routeDefinition) {
        routeDefinition.setId(routeId);
        return dynamicRouteService.updateRoute(routeDefinition)
                .thenReturn(ResponseEntity.ok("Route updated successfully"))
                .onErrorReturn(ResponseEntity.badRequest().body("Failed to update route"));
    }

    @DeleteMapping("/routes/{routeId}")
    @Operation(summary = "删除路由", description = "从网关删除指定的路由")
    public Mono<ResponseEntity<String>> deleteRoute(
            @Parameter(description = "路由ID") @PathVariable String routeId) {
        return dynamicRouteService.deleteRoute(routeId)
                .thenReturn(ResponseEntity.ok("Route deleted successfully"))
                .onErrorReturn(ResponseEntity.badRequest().body("Failed to delete route"));
    }

    @GetMapping("/routes/statistics")
    @Operation(summary = "获取路由统计", description = "获取网关路由的统计信息")
    public Mono<ResponseEntity<Map<String, Object>>> getRouteStatistics() {
        return Mono.just(ResponseEntity.ok(dynamicRouteService.getRouteStatistics()));
    }

    @GetMapping("/health/services")
    @Operation(summary = "获取服务健康状态", description = "获取所有微服务的健康状态")
    public Mono<ResponseEntity<Map<String, HealthCheckService.ServiceHealth>>> getServicesHealth() {
        return Mono.just(ResponseEntity.ok(healthCheckService.getAllServicesHealth()));
    }

    @GetMapping("/health/services/{serviceId}")
    @Operation(summary = "获取指定服务健康状态", description = "获取指定微服务的健康状态")
    public Mono<ResponseEntity<HealthCheckService.ServiceHealth>> getServiceHealth(
            @Parameter(description = "服务ID") @PathVariable String serviceId) {
        HealthCheckService.ServiceHealth health = healthCheckService.getServiceHealth(serviceId);
        return Mono.just(ResponseEntity.ok(health));
    }

    @GetMapping("/health/statistics")
    @Operation(summary = "获取健康统计", description = "获取服务健康状态统计信息")
    public Mono<ResponseEntity<Map<String, Object>>> getHealthStatistics() {
        return Mono.just(ResponseEntity.ok(healthCheckService.getHealthStatistics()));
    }

    @PostMapping("/token/validate")
    @Operation(summary = "验证令牌", description = "验证JWT令牌的有效性")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(
            @Parameter(description = "JWT令牌") @RequestHeader("Authorization") String authHeader) {
        log.info("开始验证令牌");

        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
            log.info("提取令牌成功 - Token: {}", tokenPreview);

            Map<String, Object> tokenInfo = jwtTokenService.getTokenInfo(token);
            log.info("令牌验证成功 - Token: {}", tokenPreview);

            return Mono.just(ResponseEntity.ok(tokenInfo));
        } catch (Exception e) {
            log.error("令牌验证失败", e);
            return Mono.just(ResponseEntity.badRequest().body(Map.of("valid", false, "error", e.getMessage())));
        }
    }

    @PostMapping("/token/revoke")
    @Operation(summary = "撤销令牌", description = "撤销JWT令牌，使其失效")
    public Mono<ResponseEntity<String>> revokeToken(
            @Parameter(description = "JWT令牌") @RequestHeader("Authorization") String authHeader) {
        log.info("开始撤销令牌");

        try {
            String token = authHeader.substring(7);
            String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
            log.info("提取令牌成功 - Token: {}", tokenPreview);

            jwtTokenService.revokeToken(token);
            log.info("令牌撤销成功 - Token: {}", tokenPreview);

            return Mono.just(ResponseEntity.ok("Token revoked successfully"));
        } catch (Exception e) {
            log.error("令牌撤销失败", e);
            return Mono.just(ResponseEntity.badRequest().body("Failed to revoke token: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "获取网关状态", description = "获取网关服务的运行状态")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayStatus() {
        Map<String, Object> status = Map.of(
                "status", "RUNNING",
                "timestamp", System.currentTimeMillis(),
                "cachedRoutes", dynamicRouteService.getCachedRouteCount(),
                "servicesCount", healthCheckService.getAllServicesHealth().size()
        );
        return Mono.just(ResponseEntity.ok(status));
    }

    @PostMapping("/routes/clear-cache")
    @Operation(summary = "清空路由缓存", description = "清空网关的路由缓存")
    public Mono<ResponseEntity<String>> clearRouteCache() {
        dynamicRouteService.clearRouteCache();
        return Mono.just(ResponseEntity.ok("Route cache cleared successfully"));
    }

    @PostMapping("/health/cleanup")
    @Operation(summary = "清理健康检查缓存", description = "清理过期的健康检查缓存")
    public Mono<ResponseEntity<String>> cleanupHealthCache() {
        log.info("开始清理健康检查缓存");

        try {
            jwtTokenService.cleanupExpiredSessions();
            log.info("健康检查缓存清理成功");

            return Mono.just(ResponseEntity.ok("Health cache cleaned successfully"));
        } catch (Exception e) {
            log.error("健康检查缓存清理失败", e);
            return Mono.just(ResponseEntity.badRequest().body("Failed to cleanup health cache: " + e.getMessage()));
        }
    }
}