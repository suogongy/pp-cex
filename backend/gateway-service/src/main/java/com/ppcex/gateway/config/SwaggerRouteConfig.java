package com.ppcex.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class SwaggerRouteConfig {

    // 微服务列表，可从配置文件读取
    private static final List<String> MICROSERVICES = Arrays.asList(
            "user-service",
            "trade-service",
            "wallet-service",
            "finance-service",
            "market-service",
            "risk-service",
            "notify-service",
            "match-service"
    );

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        // ------------------ Knife4j UI 资源路由 ------------------
        routes.route("knife4j-ui-resources", r -> r.path("/webjars/**", "/swagger-ui/**", "/v3/api-docs/swagger-config")
                .filters(f -> f.preserveHostHeader())
                .uri("lb://gateway-service")
        );

        // ------------------ 微服务 Swagger 路由 ------------------
        for (String service : MICROSERVICES) {
            String pathPattern = "/" + service + "/v3/api-docs/**";
            String rewritePattern = "/" + service + "/v3/api-docs/(?<segment>.*)";

            routes.route(service + "-swagger", r -> r.path(pathPattern)
                    .filters(f -> f
                            .preserveHostHeader()
                            .rewritePath(rewritePattern, "/v3/api-docs/${segment}")
                    )
                    .uri("lb://" + service)
            );
        }

        return routes.build();
    }

    // 获取网关服务端口
    @Value("${server.port:8080}")
    private int gatewayPort;

    private int getGatewayPort() {
        return gatewayPort;
    }

    // ------------------ 判断是否为 Swagger 路径 ------------------
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean isSwaggerPath(String path) {
        List<String> swaggerPaths = getSwaggerPaths();
        for (String swaggerPath : swaggerPaths) {
            if (matcher.match(swaggerPath, path)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSwaggerPaths() {
        List<String> paths = Arrays.asList(
                "/doc.html",
                "/doc.html/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**"
        );
        for (String service : MICROSERVICES) {
            paths.add("/" + service + "/v3/api-docs/**");
        }
        return paths;
    }
}
