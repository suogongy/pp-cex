package com.ppcex.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关配置类
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    /**
     * IP限流解析器（主要解析器）
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                // 获取真实客户端IP
                String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return Mono.just(xForwardedFor.split(",")[0].trim());
                }

                String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return Mono.just(xRealIp);
                }

                return Mono.just(exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown");
            }
        };
    }

    /**
     * 用户限流解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * API路径限流解析器
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }
}