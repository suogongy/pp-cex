package com.ppcex.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关OpenAPI配置
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PPCEX Gateway Service API")
                        .description("网关服务API文档")
                        .version("v1.0.0")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("gateway")
                .pathsToMatch("/actuator/**", "/gateway/**")
                .build();
    }

    @Bean
    public GroupedOpenApi servicesApi() {
        return GroupedOpenApi.builder()
                .group("services")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}