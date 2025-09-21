package com.ppcex.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SwaggerAggregationProvider {

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final SwaggerAggregationConfig swaggerAggregationConfig;

    @Bean
    @Primary
    public Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls() {
        List<RouteDefinition> routeDefinitions = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block();

        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

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

        log.info("Swagger聚合配置完成，共发现{}个微服务", urls.size());
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