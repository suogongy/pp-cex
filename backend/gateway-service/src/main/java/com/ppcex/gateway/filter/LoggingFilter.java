package com.ppcex.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 生成请求追踪ID
        String traceId = generateTraceId();

        // 添加追踪ID到请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Trace-Id", traceId)
                .header("X-Request-Start", String.valueOf(System.currentTimeMillis()))
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // 记录请求开始
        logRequestStart(mutatedExchange, traceId);

        long startTime = System.currentTimeMillis();

        // 使用响应装饰器来添加响应头
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                // 在写入响应体之前添加响应头
                if (!getHeaders().containsKey("X-Trace-Id")) {
                    getHeaders().add("X-Trace-Id", traceId);
                }
                if (!getHeaders().containsKey("X-Response-Time")) {
                    getHeaders().add("X-Response-Time", String.valueOf(System.currentTimeMillis() - startTime));
                }
                return super.writeWith(body);
            }
        };

        ServerWebExchange finalExchange = mutatedExchange.mutate()
                .response(decoratedResponse)
                .build();

        return chain.filter(finalExchange)
                .doOnSuccess(v -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logRequestSuccess(finalExchange, decoratedResponse, traceId, duration);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logRequestError(finalExchange, traceId, duration, error);
                });
    }

    private void logRequestStart(ServerWebExchange exchange, String traceId) {
        ServerHttpRequest request = exchange.getRequest();

        Map<String, Object> logData = new HashMap<>();
        logData.put("traceId", traceId);
        logData.put("method", request.getMethod().name());
        logData.put("path", request.getPath().value());
        logData.put("query", request.getURI().getQuery());
        logData.put("clientIp", getClientIp(request));
        logData.put("userAgent", request.getHeaders().getFirst("User-Agent"));
        logData.put("userId", request.getHeaders().getFirst("X-User-Id"));
        logData.put("timestamp", Instant.now().toString());
        logData.put("type", "REQUEST_START");

        log.info("Gateway Request Start: {}", com.alibaba.fastjson2.JSON.toJSONString(logData));
    }

    private void logRequestSuccess(ServerWebExchange exchange, ServerHttpResponse response, String traceId, long duration) {
        ServerHttpRequest request = exchange.getRequest();

        Map<String, Object> logData = new HashMap<>();
        logData.put("traceId", traceId);
        logData.put("method", request.getMethod().name());
        logData.put("path", request.getPath().value());
        logData.put("statusCode", response.getStatusCode() != null ? response.getStatusCode().value() : 0);
        logData.put("duration", duration);
        logData.put("userId", request.getHeaders().getFirst("X-User-Id"));
        logData.put("timestamp", Instant.now().toString());
        logData.put("type", "REQUEST_SUCCESS");

        log.info("Gateway Request Success: {}", com.alibaba.fastjson2.JSON.toJSONString(logData));

        // 记录慢请求（超过500ms）
        if (duration > 500) {
            log.warn("Slow request detected: {} {} - {}ms", request.getMethod(), request.getPath(), duration);
        }
    }

    private void logRequestError(ServerWebExchange exchange, String traceId, long duration, Throwable error) {
        ServerHttpRequest request = exchange.getRequest();

        Map<String, Object> logData = new HashMap<>();
        logData.put("traceId", traceId);
        logData.put("method", request.getMethod().name());
        logData.put("path", request.getPath().value());
        logData.put("duration", duration);
        logData.put("error", error.getMessage());
        logData.put("errorType", error.getClass().getSimpleName());
        logData.put("userId", request.getHeaders().getFirst("X-User-Id"));
        logData.put("timestamp", Instant.now().toString());
        logData.put("type", "REQUEST_ERROR");

        log.error("Gateway Request Error: {}", com.alibaba.fastjson2.JSON.toJSONString(logData), error);
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

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    @Override
    public int getOrder() {
        return -110; // 在认证过滤器之前执行，确保所有请求都被记录
    }
}