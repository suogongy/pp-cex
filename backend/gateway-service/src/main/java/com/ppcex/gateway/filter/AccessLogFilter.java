package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.ppcex.gateway.model.AccessLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 访问日志过滤器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // 生成请求ID和追踪ID
        String requestId = generateRequestId();
        String traceId = generateTraceId();

        // 添加到请求属性中，供后续使用
        exchange.getAttributes().put("requestId", requestId);
        exchange.getAttributes().put("traceId", traceId);
        exchange.getAttributes().put("startTime", startTime);

        // 添加追踪ID到响应头
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("X-Request-Id", requestId);
        response.getHeaders().add("X-Trace-Id", traceId);

        // 异步记录请求日志
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long endTime = System.currentTimeMillis();
                    logAccess(exchange, response, endTime - endTime);
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    logAccess(exchange, response, endTime - startTime);
                    log.error("Request processing failed", error);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // 最低优先级，最后执行
    }

    /**
     * 记录访问日志
     */
    private void logAccess(ServerWebExchange exchange, ServerHttpResponse response, long responseTime) {
        try {
            ServerHttpRequest request = exchange.getRequest();

            AccessLog accessLog = new AccessLog();
            accessLog.setId(UUID.randomUUID().toString());
            accessLog.setRequestId((String) exchange.getAttributes().get("requestId"));
            accessLog.setTraceId((String) exchange.getAttributes().get("traceId"));

            // 用户信息
            String userId = request.getHeaders().getFirst("X-User-Id");
            String username = request.getHeaders().getFirst("X-Username");
            accessLog.setUserId(userId);

            // 请求信息
            accessLog.setClientIp(getClientIp(request));
            accessLog.setMethod(request.getMethod().name());
            accessLog.setPath(request.getPath().value());
            accessLog.setProtocol(request.getURI().getScheme());
            accessLog.setRequestTime(LocalDateTime.now());

            // 请求参数
            Map<String, String> parameters = new HashMap<>();
            request.getQueryParams().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    parameters.put(key, values.get(0));
                }
            });
            accessLog.setParameters(parameters);

            // 响应信息
            accessLog.setResponseStatus(response.getStatusCode() != null ? response.getStatusCode().value() : 500);
            accessLog.setDuration(responseTime);
            accessLog.setResponseTime(LocalDateTime.now());

            // 用户代理
            String userAgent = request.getHeaders().getFirst("User-Agent");
            accessLog.setUserAgent(StringUtils.hasText(userAgent) ? userAgent.substring(0, Math.min(userAgent.length(), 255)) : "");

            // 服务名称（从路径推断）
            accessLog.setServiceName(extractServiceName(request.getPath().value()));

            // 异步保存到Redis
            asyncSaveAccessLog(accessLog);

            // 异步发送到ELK
            asyncSendToELK(accessLog);

            log.debug("Access log recorded: {} {} {} {}ms",
                request.getMethod(), request.getPath(), response.getStatusCode(), responseTime);

        } catch (Exception e) {
            log.error("Failed to record access log", e);
        }
    }

    /**
     * 异步保存访问日志到Redis
     */
    private void asyncSaveAccessLog(AccessLog accessLog) {
        try {
            // 保存到列表中，用于短期查询
            String key = "access:log:" + accessLog.getRequestTime().toLocalDate().toString();
            redisTemplate.opsForList().rightPush(key, accessLog);

            // 设置过期时间（7天）
            redisTemplate.expire(key, 7, java.util.concurrent.TimeUnit.DAYS);

            // 保存统计信息
            updateAccessStatistics(accessLog);

        } catch (Exception e) {
            log.error("Failed to save access log to Redis", e);
        }
    }

    /**
     * 更新访问统计信息
     */
    private void updateAccessStatistics(AccessLog accessLog) {
        try {
            String dateKey = "access:stats:" + accessLog.getRequestTime().toLocalDate().toString();
            String hourKey = dateKey + ":" + accessLog.getRequestTime().getHour();

            // 总请求量
            redisTemplate.opsForValue().increment(hourKey + ":total");

            // 成功/失败请求
            if (accessLog.getResponseStatus() >= 200 && accessLog.getResponseStatus() < 300) {
                redisTemplate.opsForValue().increment(hourKey + ":success");
            } else {
                redisTemplate.opsForValue().increment(hourKey + ":failed");
            }

            // 按服务统计
            if (StringUtils.hasText(accessLog.getServiceName())) {
                redisTemplate.opsForValue().increment(hourKey + ":service:" + accessLog.getServiceName());
            }

            // 响应时间统计
            redisTemplate.opsForList().rightPush(hourKey + ":response-time", accessLog.getResponseTime());

            // 设置过期时间（30天）
            redisTemplate.expire(dateKey, 30, java.util.concurrent.TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("Failed to update access statistics", e);
        }
    }

    /**
     * 异步发送到ELK
     */
    private void asyncSendToELK(AccessLog accessLog) {
        try {
            // 这里可以实现发送到ELK的逻辑
            // 例如通过Logstash或Elasticsearch API
            String logMessage = JSON.toJSONString(accessLog);
            log.info("Sending to ELK: {}", logMessage);

        } catch (Exception e) {
            log.error("Failed to send access log to ELK", e);
        }
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
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
     * 从路径提取服务名称
     */
    private String extractServiceName(String path) {
        try {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return parts[3]; // /api/v1/service-name/...
            }
        } catch (Exception e) {
            log.error("Failed to extract service name from path: {}", path, e);
        }
        return "unknown";
    }
}