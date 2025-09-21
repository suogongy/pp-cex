package com.ppcex.gateway.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpenApiAggregationConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigParameters swaggerUiConfigParameters;

    public OpenApiAggregationConfig(DiscoveryClient discoveryClient,
                                    SwaggerUiConfigParameters swaggerUiConfigParameters) {
        this.discoveryClient = discoveryClient;
        this.swaggerUiConfigParameters = swaggerUiConfigParameters;
    }

    @PostConstruct
    public void init() {
        // 从 Nacos 获取注册的所有服务
        discoveryClient.getServices().stream()
                .filter(s -> !s.equals("gateway-service"))
                .forEach(serviceName -> {
                    log.info("发现服务: {}", serviceName);
                    // 每个服务创建一个 Swagger 分组
                    swaggerUiConfigParameters.addGroup(serviceName);
                });
    }
}
