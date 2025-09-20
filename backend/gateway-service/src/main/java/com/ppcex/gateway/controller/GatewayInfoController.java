package com.ppcex.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关信息控制器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/gateway")
public class GatewayInfoController {

    /**
     * 获取网关基本信息
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "PPCEX Gateway Service");
        info.put("version", "1.0.0");
        info.put("description", "网关服务，负责路由转发和负载均衡");
        info.put("status", "RUNNING");
        return Mono.just(info);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        return Mono.just(health);
    }
}