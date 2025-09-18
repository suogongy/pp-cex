package com.ppcex.risk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI riskServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PPCEX 风控服务 API")
                        .description("风控服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PPCEX Team")
                                .email("support@ppcex.com")
                                .url("https://www.ppcex.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    @Bean
    public GroupedOpenApi riskApi() {
        return GroupedOpenApi.builder()
                .group("risk-service")
                .pathsToMatch("/risk/api/**")
                .build();
    }
}