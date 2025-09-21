package com.ppcex.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class Knife4jConfig {

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final SwaggerAggregationConfig swaggerAggregationConfig;

    @Bean
    public List<GroupedOpenApi> groupedOpenApis() {
        List<GroupedOpenApi> groups = new ArrayList<>();

        // 添加网关自身的API组
        groups.add(GroupedOpenApi.builder()
                .group("网关服务")
                .pathsToMatch("/api/**")
                .build());

        // 添加各个微服务的API组
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        if (routes != null) {
            routes.stream()
                    .filter(route -> route.getId() != null && route.getId().endsWith("-service"))
                    .sorted(Comparator.comparing(RouteDefinition::getId))
                    .forEach(route -> {
                        String routeId = route.getId();
                        if (swaggerAggregationConfig.isServiceSupported(routeId)) {
                            // 从路由ID中提取真实服务名
                            String serviceName = routeId;
                            if (routeId.contains("_")) {
                                serviceName = routeId.substring(routeId.lastIndexOf("_") + 1);
                            }
                            String servicePath = "/" + serviceName.replace("-service", "");
                            groups.add(GroupedOpenApi.builder()
                                    .group(serviceName)
                                    .pathsToMatch(servicePath + "/**")
                                    .build());
                            log.info("添加API组 - 服务: {}, 路径: {}", serviceName, servicePath);
                        }
                    });
        }

        log.info("共创建 {} 个API组", groups.size());
        return groups;
    }
}