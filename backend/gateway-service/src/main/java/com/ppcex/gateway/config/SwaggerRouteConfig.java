package com.ppcex.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SwaggerRouteConfig {

    // Swagger文档路由配置
    private static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    private static final String SWAGGER_RESOURCES_PATH = "/swagger-resources/**";
    private static final String V3_API_DOCS_PATH = "/v3/api-docs/**";
    private static final String WEB_JARS_PATH = "/webjars/**";

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Swagger UI路由
                .route("swagger-ui", r -> r.path(SWAGGER_UI_PATH)
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/swagger-ui/(?<segment>.*)", "/swagger-ui/${segment}")
                        )
                        .uri("lb://gateway-service")
                )

                // Swagger资源路由
                .route("swagger-resources", r -> r.path(SWAGGER_RESOURCES_PATH)
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/swagger-resources/(?<segment>.*)", "/swagger-resources/${segment}")
                        )
                        .uri("lb://gateway-service")
                )

                // v3 API文档路由
                .route("v3-api-docs", r -> r.path(V3_API_DOCS_PATH)
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/v3/api-docs/(?<serviceId>.*?)/(?<segment>.*)", "/${serviceId}/v3/api-docs/${segment}")
                                .rewritePath("/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://gateway-service")
                )

                // WebJars路由
                .route("webjars", r -> r.path(WEB_JARS_PATH)
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/webjars/(?<segment>.*)", "/webjars/${segment}")
                        )
                        .uri("lb://gateway-service")
                )

                // Knife4j文档路由
                .route("knife4j-docs", r -> r.path("/doc.html**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/doc.html/(?<segment>.*)", "/doc.html")
                        )
                        .uri("lb://gateway-service")
                )

                // 用户服务Swagger路由
                .route("user-service-swagger", r -> r.path("/user/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/user/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://user-service")
                )

                // 交易服务Swagger路由
                .route("trade-service-swagger", r -> r.path("/trade/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/trade/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://trade-service")
                )

                // 钱包服务Swagger路由
                .route("wallet-service-swagger", r -> r.path("/wallet/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/wallet/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://wallet-service")
                )

                // 财务服务Swagger路由
                .route("finance-service-swagger", r -> r.path("/finance/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/finance/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://finance-service")
                )

                // 行情服务Swagger路由
                .route("market-service-swagger", r -> r.path("/market/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/market/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://market-service")
                )

                // 风控服务Swagger路由
                .route("risk-service-swagger", r -> r.path("/risk/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/risk/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://risk-service")
                )

                // 通知服务Swagger路由
                .route("notify-service-swagger", r -> r.path("/notify/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/notify/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://notify-service")
                )

                // 撮合服务Swagger路由
                .route("match-service-swagger", r -> r.path("/match/v3/api-docs/**")
                        .filters(f -> f
                                .preserveHostHeader()
                                .rewritePath("/match/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}")
                        )
                        .uri("lb://match-service")
                )

                .build();
    }

    /**
     * 获取所有Swagger相关的路由路径
     */
    public static String[] getSwaggerPaths() {
        return new String[]{
                SWAGGER_UI_PATH,
                SWAGGER_RESOURCES_PATH,
                V3_API_DOCS_PATH,
                WEB_JARS_PATH,
                "/doc.html**",
                "/user/v3/api-docs/**",
                "/trade/v3/api-docs/**",
                "/wallet/v3/api-docs/**",
                "/finance/v3/api-docs/**",
                "/market/v3/api-docs/**",
                "/risk/v3/api-docs/**",
                "/notify/v3/api-docs/**",
                "/match/v3/api-docs/**"
        };
    }

    /**
     * 检查路径是否为Swagger相关路径
     */
    public static boolean isSwaggerPath(String path) {
        for (String swaggerPath : getSwaggerPaths()) {
            if (path.matches(swaggerPath.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }
}