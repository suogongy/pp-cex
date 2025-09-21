package com.ppcex.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SwaggerAggregationConfig {

    private final RouteDefinitionLocator routeDefinitionLocator;

    // 微服务Swagger文档路径映射
    private static final String SWAGGER_URL = "/v3/api-docs";
    private static final Set<String> SWAGGER_SERVICES = new HashSet<>(List.of(
            "user-service", "trade-service", "wallet-service", "finance-service",
            "market-service", "risk-service", "notify-service", "match-service"
    ));

    /**
     * 获取路由的Swagger文档位置
     */
    private String getSwaggerLocation(RouteDefinition route) {
        try {
            String routeId = route.getId();
            String uri = route.getUri().toString();

            // 只处理lb://开头的服务发现路由
            if (!uri.startsWith("lb://")) {
                return null;
            }

            // 构建Swagger文档URL
            String servicePath = route.getPredicates().stream()
                    .filter(predicate -> predicate.toString().contains("Path="))
                    .findFirst()
                    .map(predicate -> {
                        String path = predicate.toString();
                        path = path.substring(path.indexOf("=") + 1, path.indexOf(","));
                        path = path.replace("**", "");
                        return path;
                    })
                    .orElse("");

            return servicePath + SWAGGER_URL;
        } catch (Exception e) {
            log.error("Error getting swagger location for route: {}", route.getId(), e);
            return null;
        }
    }

    /**
     * 获取服务的Swagger文档URL
     */
    public String getServiceSwaggerUrl(String serviceName) {
        if (!SWAGGER_SERVICES.contains(serviceName)) {
            throw new IllegalArgumentException("Service not supported: " + serviceName);
        }

        return "/" + serviceName.replace("-service", "") + SWAGGER_URL;
    }

    /**
     * 检查服务是否支持Swagger文档
     */
    public boolean isServiceSupported(String serviceName) {
        return SWAGGER_SERVICES.contains(serviceName);
    }

    /**
     * 获取所有支持Swagger文档的服务
     */
    public Set<String> getSupportedServices() {
        return new HashSet<>(SWAGGER_SERVICES);
    }
}