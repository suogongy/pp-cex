package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.ppcex.common.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证过滤器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 无需认证的路径 */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/public/**",
        "/actuator/**",
        "/doc.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/error"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("Authentication filter processing request: {}", path);

        // 检查是否为公共路径
        if (isPublicPath(path)) {
            log.debug("Path {} is public, skipping authentication", path);
            return chain.filter(exchange);
        }

        // 提取JWT令牌
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("No token found for path: {}", path);
            return unauthorized(exchange.getResponse(), "未登录或令牌已过期");
        }

        // 验证JWT令牌
        try {
            Claims claims = jwtService.parseToken(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            log.debug("User {} authenticated for path: {}", username, path);

            // 检查用户状态
            String userStatusKey = "user:status:" + userId;
            Object userStatus = redisTemplate.opsForValue().get(userStatusKey);
            if (userStatus != null && "DISABLED".equals(userStatus.toString())) {
                log.warn("User {} is disabled", username);
                return forbidden(exchange.getResponse(), "用户账户已被禁用");
            }

            // 检查令牌黑名单
            String tokenBlacklistKey = "token:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(tokenBlacklistKey))) {
                log.warn("Token {} is in blacklist", token.substring(0, 20) + "...");
                return unauthorized(exchange.getResponse(), "令牌已失效");
            }

            // 添加用户信息到请求头
            ServerHttpRequest.Builder builder = request.mutate();
            builder.header("X-User-Id", userId);
            builder.header("X-Username", username);
            builder.header("X-Roles", claims.get("roles", String.class));

            ServerHttpRequest newRequest = builder.build();
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            return chain.filter(newExchange);

        } catch (Exception e) {
            log.error("Token validation failed for path: {}", path, e);
            return unauthorized(exchange.getResponse(), "令牌验证失败");
        }
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }

    /**
     * 检查是否为公共路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 提取JWT令牌
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(
            JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 返回禁止访问响应
     */
    private Mono<Void> forbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 403);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(
            JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}