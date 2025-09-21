package com.ppcex.gateway.controller;

import com.ppcex.gateway.filter.MonitoringFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
@Tag(name = "监控信息", description = "网关服务监控信息接口")
@RequiredArgsConstructor
public class GatewayInfoController {

    private final MonitoringFilter monitoringFilter;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @GetMapping("/info")
    @Operation(summary = "获取应用信息", description = "获取网关服务的基本信息")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("description", "PPCEX Gateway Service");
        info.put("startTime", Instant.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));

        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "网关服务健康检查接口")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", applicationName);
        health.put("version", applicationVersion);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics")
    @Operation(summary = "获取监控指标", description = "获取网关服务的监控指标")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = monitoringFilter.getMonitoringData();
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("service", applicationName);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/ping")
    @Operation(summary = "心跳检测", description = "网关服务心跳检测接口")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}