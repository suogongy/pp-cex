package com.ppcex.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 网关OpenAPI聚合配置
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class GatewayOpenApiConfig {

    private final DiscoveryClient discoveryClient;

    /**
     * 网关自身API文档
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PPCEX Gateway API")
                        .version("1.0.0")
                        .description("网关服务API文档")
                        .contact(new Contact()
                                .name("PPCEX Team")
                                .email("support@ppcex.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    /**
     * 网关API分组
     */
    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("gateway")
                .displayName("网关服务")
                .pathsToMatch("/api/v1/gateway/**", "/actuator/**")
                .build();
    }

    /**
     * 动态创建各微服务的API分组
     */
    @Bean
    public List<GroupedOpenApi> serviceApis() {
        List<String> services = discoveryClient.getServices();
        log.info("发现服务数量: {}", services.size());

        return services.stream()
                .filter(serviceName -> !serviceName.equals("gateway-service"))
                .map(serviceName -> {
                    log.info("为服务创建API分组: {}", serviceName);
                    return GroupedOpenApi.builder()
                            .group(serviceName)
                            .displayName(serviceName.replace("-service", "").toUpperCase() + "服务")
                            .pathsToMatch("/" + serviceName + "/**")
                            .build();
                })
                .toList();
    }
}