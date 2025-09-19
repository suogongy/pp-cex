package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流过滤器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 限流Lua脚本 */
    private static final String RATE_LIMIT_SCRIPT = """
        local key = KEYS[1]
        local limit = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local current = redis.call('GET', key)

        if current == false then
            redis.call('SET', key, 1)
            redis.call('EXPIRE', key, window)
            return 1
        else
            current = tonumber(current)
            if current < limit then
                redis.call('INCR', key)
                return 1
            else
                return 0
            end
        end
        """;

    private final DefaultRedisScript<Long> rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);

    /** 无需限流的路径 */
    private static final List<String> BYPASS_PATHS = Arrays.asList(
        "/actuator/**",
        "/doc.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);

        log.debug("Rate limit filter processing request: {} from {}", path, clientIp);

        // 检查是否需要跳过限流
        if (shouldBypassRateLimit(path)) {
            return chain.filter(exchange);
        }

        // IP限流检查
        if (isIpRateLimited(clientIp)) {
            log.warn("IP {} rate limited for path: {}", clientIp, path);
            return tooManyRequests(exchange.getResponse(), "请求过于频繁，请稍后重试");
        }

        // 用户限流检查
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && isUserRateLimited(userId)) {
            log.warn("User {} rate limited for path: {}", userId, path);
            return tooManyRequests(exchange.getResponse(), "用户请求过于频繁，请稍后重试");
        }

        // API限流检查
        if (isApiRateLimited(path)) {
            log.warn("API {} rate limited", path);
            return tooManyRequests(exchange.getResponse(), "接口请求过于频繁，请稍后重试");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90; // 在认证过滤器之后
    }

    /**
     * 检查是否需要跳过限流
     */
    private boolean shouldBypassRateLimit(String path) {
        return BYPASS_PATHS.stream().anyMatch(pattern -> path.startsWith(pattern.replace("/**", "")));
    }

    /**
     * IP限流检查
     */
    private boolean isIpRateLimited(String clientIp) {
        String key = "rate:limit:ip:" + clientIp;
        return checkRateLimit(key, 1000, 60); // 1000 requests per minute
    }

    /**
     * 用户限流检查
     */
    private boolean isUserRateLimited(String userId) {
        String key = "rate:limit:user:" + userId;
        return checkRateLimit(key, 500, 60); // 500 requests per minute
    }

    /**
     * API限流检查
     */
    private boolean isApiRateLimited(String path) {
        // 不同API路径的不同限流配置
        if (path.startsWith("/api/v1/market/")) {
            String key = "rate:limit:api:market";
            return checkRateLimit(key, 5000, 60); // 5000 requests per minute
        } else if (path.startsWith("/api/v1/trade/")) {
            String key = "rate:limit:api:trade";
            return checkRateLimit(key, 200, 60); // 200 requests per minute
        } else if (path.startsWith("/api/v1/wallet/")) {
            String key = "rate:limit:api:wallet";
            return checkRateLimit(key, 100, 60); // 100 requests per minute
        } else {
            String key = "rate:limit:api:other";
            return checkRateLimit(key, 1000, 60); // 1000 requests per minute
        }
    }

    /**
     * 检查限流
     */
    private boolean checkRateLimit(String key, int limit, int windowSeconds) {
        try {
            Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds)
            );
            return result != null && result == 0;
        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}", key, e);
            return false; // 限流检查失败，默认允许通过
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 返回请求过多响应
     */
    private Mono<Void> tooManyRequests(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 429);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(
            JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}