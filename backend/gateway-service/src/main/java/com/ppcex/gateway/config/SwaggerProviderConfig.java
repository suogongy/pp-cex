package com.ppcex.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerProviderConfig {

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final SwaggerAggregationConfig swaggerAggregationConfig;

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();

        // 获取所有路由并配置Swagger UI
        List<RouteDefinition> routeDefinitions = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block();

        if (routeDefinitions != null) {
            List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new ArrayList<>();

            for (RouteDefinition routeDefinition : routeDefinitions) {
                String routeId = routeDefinition.getId();

                // 检查是否为支持Swagger的服务
                if (swaggerAggregationConfig.isServiceSupported(routeId)) {
                    String serviceName = routeId;
                    if (routeId.contains("_")) {
                        serviceName = routeId.substring(routeId.lastIndexOf("_") + 1);
                    }

                    // 添加网关自身的API文档
                    if (serviceName.equals("gateway-service")) {
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                                "gateway",
                                "/v3/api-docs",
                                "网关服务"
                        ));
                    } else {
                        // 添加微服务的API文档
                        String servicePath = "/" + serviceName.replace("-service", "");
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                                serviceName.replace("-service", ""),
                                servicePath + "/v3/api-docs",
                                getServiceDisplayName(serviceName)
                        ));
                    }
                }
            }

            properties.setUrls(new HashSet<>(urls));
        }

        return properties;
    }

    @Bean
    public List<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls() {
        List<RouteDefinition> routeDefinitions = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block();

        List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new ArrayList<>();

        if (routeDefinitions != null) {
            for (RouteDefinition routeDefinition : routeDefinitions) {
                String routeId = routeDefinition.getId();

                if (swaggerAggregationConfig.isServiceSupported(routeId)) {
                    String serviceName = routeId;
                    if (routeId.contains("_")) {
                        serviceName = routeId.substring(routeId.lastIndexOf("_") + 1);
                    }

                    if (serviceName.equals("gateway-service")) {
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                                "gateway",
                                "/v3/api-docs",
                                "网关服务"
                        ));
                    } else {
                        String servicePath = "/" + serviceName.replace("-service", "");
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                                serviceName.replace("-service", ""),
                                servicePath + "/v3/api-docs",
                                getServiceDisplayName(serviceName)
                        ));
                    }
                }
            }
        }

        return urls;
    }

    private String getServiceDisplayName(String serviceName) {
        switch (serviceName) {
            case "user-service":
                return "用户服务";
            case "trade-service":
                return "交易服务";
            case "wallet-service":
                return "钱包服务";
            case "finance-service":
                return "财务服务";
            case "market-service":
                return "行情服务";
            case "risk-service":
                return "风控服务";
            case "notify-service":
                return "通知服务";
            case "match-service":
                return "撮合服务";
            default:
                return serviceName;
        }
    }
}