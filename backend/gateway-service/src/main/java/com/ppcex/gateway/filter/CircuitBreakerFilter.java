package com.ppcex.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class CircuitBreakerFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    // 简化的熔断器状态管理
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    // 熔断器配置
    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration OPEN_TIMEOUT = Duration.ofSeconds(30);
    private static final int HALF_OPEN_MAX_REQUESTS = 3;
    private static final Duration HALF_OPEN_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 获取目标服务
        String serviceName = extractServiceName(path);
        if (serviceName == null) {
            return chain.filter(exchange);
        }

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());

        // 检查熔断器状态
        if (state.getState() == CircuitBreakerState.State.OPEN) {
            if (System.currentTimeMillis() - state.getLastFailureTime() > OPEN_TIMEOUT.toMillis()) {
                state.transitionToHalfOpen();
            } else {
                return handleFallback(exchange.getResponse(), serviceName, "Service circuit breaker is open");
            }
        }

        if (state.getState() == CircuitBreakerState.State.HALF_OPEN) {
            if (state.getHalfOpenRequests().get() >= HALF_OPEN_MAX_REQUESTS) {
                return handleFallback(exchange.getResponse(), serviceName, "Service in half-open state, max requests exceeded");
            }
        }

        // 执行请求
        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    state.recordSuccess();
                    log.debug("Request successful for service: {}, path: {}", serviceName, path);
                })
                .doOnError(error -> {
                    state.recordFailure();
                    log.error("Request failed for service: {}, path: {}, error: {}", serviceName, path, error.getMessage());

                    if (state.getState() == CircuitBreakerState.State.OPEN) {
                        log.warn("Circuit breaker opened for service: {}", serviceName);
                    }
                })
                .onErrorResume(error -> {
                    if (state.getState() == CircuitBreakerState.State.OPEN) {
                        return handleFallback(exchange.getResponse(), serviceName, "Service unavailable - circuit breaker open");
                    }
                    return Mono.error(error);
                });
    }

    private String extractServiceName(String path) {
        if (path.startsWith("/api/v1/user/")) return "user-service";
        if (path.startsWith("/api/v1/trade/")) return "trade-service";
        if (path.startsWith("/api/v1/wallet/")) return "wallet-service";
        if (path.startsWith("/api/v1/finance/")) return "finance-service";
        if (path.startsWith("/api/v1/market/")) return "market-service";
        if (path.startsWith("/api/v1/risk/")) return "risk-service";
        if (path.startsWith("/api/v1/notify/")) return "notify-service";
        if (path.startsWith("/api/v1/match/")) return "match-service";
        return null;
    }

    private Mono<Void> handleFallback(ServerHttpResponse response, String serviceName, String message) {
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("code", 503);
        fallbackResponse.put("message", message);
        fallbackResponse.put("service", serviceName);
        fallbackResponse.put("fallbackTime", System.currentTimeMillis());
        fallbackResponse.put("retryAfter", 5);

        String responseBody = com.alibaba.fastjson2.JSON.toJSONString(fallbackResponse);
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());

        log.warn("Fallback response for service: {}, message: {}", serviceName, message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -70; // 在限流过滤器之后执行
    }

    // 熔断器状态类
    private static class CircuitBreakerState {
        private enum State {
            CLOSED, OPEN, HALF_OPEN
        }

        private State state = State.CLOSED;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger halfOpenRequests = new AtomicInteger(0);
        private long lastFailureTime = 0;

        public synchronized void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();

            if (state == State.HALF_OPEN) {
                transitionToOpen();
            } else if (state == State.CLOSED && failureCount.get() >= FAILURE_THRESHOLD) {
                transitionToOpen();
            }
        }

        public synchronized void recordSuccess() {
            if (state == State.HALF_OPEN) {
                halfOpenRequests.incrementAndGet();
                if (halfOpenRequests.get() >= HALF_OPEN_MAX_REQUESTS) {
                    transitionToClosed();
                }
            } else if (state == State.CLOSED) {
                failureCount.set(0);
            }
        }

        private synchronized void transitionToOpen() {
            state = State.OPEN;
            log.info("Circuit breaker transitioned to OPEN state");
        }

        private synchronized void transitionToClosed() {
            state = State.CLOSED;
            failureCount.set(0);
            halfOpenRequests.set(0);
            log.info("Circuit breaker transitioned to CLOSED state");
        }

        private synchronized void transitionToHalfOpen() {
            state = State.HALF_OPEN;
            halfOpenRequests.set(0);
            log.info("Circuit breaker transitioned to HALF_OPEN state");
        }

        public State getState() {
            return state;
        }

        public long getLastFailureTime() {
            return lastFailureTime;
        }

        public AtomicInteger getHalfOpenRequests() {
            return halfOpenRequests;
        }
    }
}