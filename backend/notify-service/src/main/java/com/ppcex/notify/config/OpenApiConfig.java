package com.ppcex.notify.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notifyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CEX通知服务API")
                        .description("通知服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PPCEX Team")
                                .email("dev@ppcex.com")
                                .url("https://www.ppcex.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public GroupedOpenApi notifyApi() {
        return GroupedOpenApi.builder()
                .group("notify")
                .pathsToMatch("/api/notify/**")
                .build();
    }
}