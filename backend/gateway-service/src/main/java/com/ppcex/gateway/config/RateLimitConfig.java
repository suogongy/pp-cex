package com.ppcex.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Autowired
    private GatewayConfig gatewayConfig;

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId == null) {
                userId = exchange.getRequest().getHeaders().getFirst("X-User-Name");
            }
            return userId != null ? Mono.just(userId) : Mono.just("anonymous");
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress() != null ?
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
        );
    }

    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getPath().value()
        );
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            return apiKey != null ? Mono.just(apiKey) : Mono.just("no-api-key");
        };
    }

    @Bean
    public GatewayConfig.RateLimit gatewayRateLimitConfig() {
        return gatewayConfig.getRateLimit();
    }
}