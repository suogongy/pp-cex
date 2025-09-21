package com.ppcex.gateway.config;

import com.ppcex.gateway.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class GatewayWebConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HealthCheckService healthCheckService(
            org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient,
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
            RestTemplate restTemplate) {

        HealthCheckService healthCheckService = new HealthCheckService(discoveryClient, redisTemplate, restTemplate);
        healthCheckService.init();
        return healthCheckService;
    }
}