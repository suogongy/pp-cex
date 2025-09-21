package com.ppcex.gateway.filter;

import com.ppcex.gateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayConfig gatewayConfig;
    private final GatewayConfig.RateLimit rateLimitConfig;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String IP_RATE_LIMIT_PREFIX = "ip_rate_limit:";
    private static final String USER_RATE_LIMIT_PREFIX = "user_rate_limit:";
    private static final String API_RATE_LIMIT_PREFIX = "api_rate_limit:";

    // Lua脚本用于原子性的限流操作
    private DefaultRedisScript<Long> rateLimitScript;

    @PostConstruct
    public void init() {
        String luaScript = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current = redis.call('GET', key)

            if current == false then
                redis.call('SET', key, 1, 'EX', window)
                return 1
            end

            if tonumber(current) < limit then
                redis.call('INCR', key)
                return 1
            else
                return 0
            end
            """;

        rateLimitScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String clientIp = getClientIp(request);

        // 获取用户ID
        String userId = request.getHeaders().getFirst("X-User-Id");

        // 1. IP限流
        if (!checkIpRateLimit(clientIp)) {
            return handleError(exchange.getResponse(), 429, "IP rate limit exceeded");
        }

        // 2. 用户限流
        if (userId != null && !checkUserRateLimit(userId)) {
            return handleError(exchange.getResponse(), 429, "User rate limit exceeded");
        }

        // 3. API限流
        if (!checkApiRateLimit(path, method)) {
            return handleError(exchange.getResponse(), 429, "API rate limit exceeded");
        }

        // 4. 总体限流
        if (!checkGlobalRateLimit()) {
            return handleError(exchange.getResponse(), 429, "Global rate limit exceeded");
        }

        return chain.filter(exchange).doOnSuccess(v -> {
            // 记录成功的请求
            logRequestSuccess(path, method, clientIp, userId);
        }).doOnError(error -> {
            // 记录失败的请求
            logRequestError(path, method, clientIp, userId, error);
        });
    }

    private boolean checkIpRateLimit(String ip) {
        String key = IP_RATE_LIMIT_PREFIX + ip;
        return executeRateLimitScript(key,
                rateLimitConfig.getIpReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());
    }

    private boolean checkUserRateLimit(String userId) {
        String key = USER_RATE_LIMIT_PREFIX + userId;
        return executeRateLimitScript(key,
                rateLimitConfig.getUserReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());
    }

    private boolean checkApiRateLimit(String path, String method) {
        String key = API_RATE_LIMIT_PREFIX + method + ":" + path;
        return executeRateLimitScript(key,
                rateLimitConfig.getDefaultReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());
    }

    private boolean checkGlobalRateLimit() {
        String key = RATE_LIMIT_PREFIX + "global";
        return executeRateLimitScript(key,
                rateLimitConfig.getDefaultReplenishRate() * 10,
                gatewayConfig.getCache().getRateLimitTtl());
    }

    private boolean executeRateLimitScript(String key, int limit, int windowSeconds) {
        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds)
        );

        return result != null && result > 0;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private void logRequestSuccess(String path, String method, String ip, String userId) {
        log.info("Request successful: {} {} - IP: {}, User: {}", method, path, ip, userId);
    }

    private void logRequestError(String path, String method, String ip, String userId, Throwable error) {
        log.error("Request error: {} {} - IP: {}, User: {}, Error: {}", method, path, ip, userId, error.getMessage());
    }

    private Mono<Void> handleError(ServerHttpResponse response, int statusCode, String message) {
        response.setStatusCode(HttpStatus.valueOf(statusCode));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("retryAfter", "1");

        String responseBody = com.alibaba.fastjson2.JSON.toJSONString(errorResponse);
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());

        log.warn("Rate limit exceeded: {}", message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -80; // 在权限过滤器之后执行
    }
}