package com.ppcex.gateway.filter;

import com.ppcex.gateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate stringRedisTemplate;
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
            -- 参数验证
            local key = KEYS[1]
            if not key then
                return -1
            end

            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])

            if not limit or not window or limit <= 0 or window <= 0 then
                return -2
            end

            -- 获取当前计数
            local current = redis.call('GET', key)

            -- 如果key不存在，设置初始值
            if current == false then
                redis.call('SET', key, 1, 'EX', window)
                return 1
            end

            -- 转换当前值为数字
            current = tonumber(current)
            if not current then
                -- 如果当前值不是数字，重置为1
                redis.call('SET', key, 1, 'EX', window)
                return 1
            end

            -- 检查是否超过限制
            if current < limit then
                redis.call('INCR', key)
                return 1
            else
                return 0
            end
            """;

        rateLimitScript = new DefaultRedisScript<>(luaScript, Long.class);
        log.info("限流Lua脚本初始化完成");
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
        log.debug("检查IP限流 - IP: {}, Key: {}, Limit: {}, TTL: {}s",
                 ip, key, rateLimitConfig.getIpReplenishRate(), gatewayConfig.getCache().getRateLimitTtl());

        boolean result = executeRateLimitScript(key,
                rateLimitConfig.getIpReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());

        log.debug("IP限流检查结果 - IP: {}, 允许: {}", ip, result);
        return result;
    }

    private boolean checkUserRateLimit(String userId) {
        String key = USER_RATE_LIMIT_PREFIX + userId;
        log.debug("检查用户限流 - 用户ID: {}, Key: {}, Limit: {}, TTL: {}s",
                 userId, key, rateLimitConfig.getUserReplenishRate(), gatewayConfig.getCache().getRateLimitTtl());

        boolean result = executeRateLimitScript(key,
                rateLimitConfig.getUserReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());

        log.debug("用户限流检查结果 - 用户ID: {}, 允许: {}", userId, result);
        return result;
    }

    private boolean checkApiRateLimit(String path, String method) {
        String key = API_RATE_LIMIT_PREFIX + method + ":" + path;
        log.debug("检查API限流 - 路径: {} {}, Key: {}, Limit: {}, TTL: {}s",
                 method, path, key, rateLimitConfig.getDefaultReplenishRate(), gatewayConfig.getCache().getRateLimitTtl());

        boolean result = executeRateLimitScript(key,
                rateLimitConfig.getDefaultReplenishRate(),
                gatewayConfig.getCache().getRateLimitTtl());

        log.debug("API限流检查结果 - 路径: {} {}, 允许: {}", method, path, result);
        return result;
    }

    private boolean checkGlobalRateLimit() {
        String key = RATE_LIMIT_PREFIX + "global";
        int globalLimit = rateLimitConfig.getDefaultReplenishRate() * 10;
        log.debug("检查全局限流 - Key: {}, Limit: {}, TTL: {}s",
                 key, globalLimit, gatewayConfig.getCache().getRateLimitTtl());

        boolean result = executeRateLimitScript(key,
                globalLimit,
                gatewayConfig.getCache().getRateLimitTtl());

        log.debug("全局限流检查结果 - Key: {}, 允许: {}", key, result);
        return result;
    }

    private boolean executeRateLimitScript(String key, int limit, int windowSeconds) {
        try {
            log.info("执行限流脚本 - Key: {}, Limit: {}, Window: {}s", key, limit, windowSeconds);

            // 参数验证
            if (key == null || key.trim().isEmpty()) {
                log.error("限流脚本参数无效 - Key为空");
                return true;
            }

            if (limit <= 0) {
                log.error("限流脚本参数无效 - Limit值{}小于等于0", limit);
                return true;
            }

            if (windowSeconds <= 0) {
                log.error("限流脚本参数无效 - Window值{}秒小于等于0", windowSeconds);
                return true;
            }

            // 记录详细的参数信息用于调试
            log.debug("限流参数详情 - Key类型: {}, Limit类型: {}, Window类型: {}, " +
                     "Key长度: {}, Limit位数: {}, Window位数: {}",
                     key.getClass().getSimpleName(),
                     ((Object)limit).getClass().getSimpleName(),
                     ((Object)windowSeconds).getClass().getSimpleName(),
                     key.length(), String.valueOf(limit).length(), String.valueOf(windowSeconds).length());

            // 确保参数类型正确
            List<String> keys = Collections.singletonList(key);

            Long result = stringRedisTemplate.execute(rateLimitScript, keys, String.valueOf(limit), String.valueOf(windowSeconds));

            log.info("限流脚本执行结果 - Key: {}, Result: {}", key, result);

            switch (result.intValue()) {
                case -1:
                    log.error("限流脚本参数错误 - Key为空或无效");
                    return true;
                case -2:
                    log.error("限流脚本参数错误 - Limit({})或Window({})无效，请检查参数值和类型", limit, windowSeconds);
                    return true;
                case 0:
                    log.debug("限流触发 - Key: {} 已达到限制", key);
                    return false;
                case 1:
                    log.debug("限流通过 - Key: {} 允许请求", key);
                    return true;
                default:
                    log.warn("限流脚本返回未知结果 - Key: {}, Result: {}", key, result);
                    return true;
            }
        } catch (Exception e) {
            log.error("限流脚本执行失败 - Key: {}, Limit: {}, Window: {}s, Error: {}",
                     key, limit, windowSeconds, e.getMessage(), e);
            // 出现异常时默认允许通过，避免误拦截
            return true;
        }
    }

    String getClientIp(ServerHttpRequest request) {
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