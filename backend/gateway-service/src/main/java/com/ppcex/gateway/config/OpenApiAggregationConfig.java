package com.ppcex.gateway.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenApiAggregationConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigParameters swaggerUiConfigParameters;
    @PostConstruct
    public void init() {
        log.info("开始初始化OpenAPI文档聚合配置...");

        // 从 Nacos 获取注册的所有服务
        List<String> services = discoveryClient.getServices();
        log.info("发现服务总数: {}", services.size());

        services.stream()
                .filter(serviceName -> !serviceName.equals("gateway-service"))
                .forEach(serviceName -> {
                    log.info("为服务配置OpenAPI分组: {}", serviceName);

                    // 获取服务实例
                    List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                    if (!instances.isEmpty()) {
                        ServiceInstance instance = instances.get(0);
                        String contextPath = instance.getMetadata().get("context-path");
                        if (contextPath == null) {
                            contextPath = "/";
                        }

                        log.info("服务 {} 实例信息: context-path={}, host={}, port={}",
                                serviceName, contextPath, instance.getHost(), instance.getPort());

                        // 配置每个服务的OpenAPI文档URL
                        String apiDocsUrl = "/" + serviceName + "/v3/api-docs";
                        swaggerUiConfigParameters.addUrl(apiDocsUrl);

                        // 添加分组
                        swaggerUiConfigParameters.addGroup(serviceName);

                        log.info("成功添加服务 {} 的OpenAPI文档URL: {}", serviceName, apiDocsUrl);
                    } else {
                        log.warn("未找到服务 {} 的实例", serviceName);
                    }
                });

        log.info("OpenAPI文档聚合配置初始化完成");
    }
}
