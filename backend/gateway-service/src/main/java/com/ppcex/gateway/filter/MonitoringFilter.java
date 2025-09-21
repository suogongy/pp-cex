package com.ppcex.gateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoringFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;

    // 监控指标
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, Timer> responseTimeTimers = new ConcurrentHashMap<>();

    // 按服务分类的监控
    private static final List<String> SERVICES = Arrays.asList(
            "user-service", "trade-service", "wallet-service", "finance-service",
            "market-service", "risk-service", "notify-service", "match-service"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().value();
        String method = request.getMethod().name();
        String serviceName = extractServiceName(path);

        long startTime = System.currentTimeMillis();

        // 增加请求计数
        incrementRequestCount(serviceName, method);

        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 500;

                    // 记录响应时间
                    recordResponseTime(serviceName, method, duration);

                    // 记录状态码
                    recordStatusCode(serviceName, statusCode);

                    // 检查慢请求
                    if (duration > 1000) {
                        log.warn("Slow request detected: {} {} - {}ms", method, path, duration);
                        incrementSlowRequestCount(serviceName);
                    }

                    log.debug("Request completed: {} {} - {}ms - {}", method, path, duration, statusCode);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;

                    // 记录错误计数
                    incrementErrorCount(serviceName, method);

                    // 记录错误响应时间
                    recordResponseTime(serviceName, method, duration);

                    log.error("Request failed: {} {} - {}ms - {}", method, path, duration, error.getMessage());
                })
                .then();
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
        return "unknown";
    }

    private void incrementRequestCount(String serviceName, String method) {
        String key = "gateway.requests.total";
        String serviceKey = "gateway.requests." + serviceName;
        String methodKey = "gateway.requests." + method;

        meterRegistry.counter(key).increment();
        meterRegistry.counter(serviceKey).increment();
        meterRegistry.counter(methodKey).increment();

        // 更新本地计数器
        requestCounts.computeIfAbsent(serviceName, k -> new AtomicLong(0)).incrementAndGet();
    }

    private void incrementErrorCount(String serviceName, String method) {
        String key = "gateway.errors.total";
        String serviceKey = "gateway.errors." + serviceName;
        String methodKey = "gateway.errors." + method;

        meterRegistry.counter(key).increment();
        meterRegistry.counter(serviceKey).increment();
        meterRegistry.counter(methodKey).increment();

        // 更新本地计数器
        errorCounts.computeIfAbsent(serviceName, k -> new AtomicLong(0)).incrementAndGet();
    }

    private void incrementSlowRequestCount(String serviceName) {
        String key = "gateway.slow_requests." + serviceName;
        meterRegistry.counter(key).increment();
    }

    private void recordResponseTime(String serviceName, String method, long duration) {
        String key = "gateway.response_time";
        String serviceKey = "gateway.response_time." + serviceName;
        String methodKey = "gateway.response_time." + method;

        // 使用Micrometer的Timer
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder(key)
                .tag("service", serviceName)
                .tag("method", method)
                .register(meterRegistry));

        // 更新本地计时器
        responseTimeTimers.computeIfAbsent(serviceName, k -> Timer.builder("service.response_time")
                .tag("service", serviceName)
                .register(meterRegistry))
                .record(Duration.ofMillis(duration));
    }

    private void recordStatusCode(String serviceName, int statusCode) {
        String key = "gateway.status_codes";
        meterRegistry.counter(key, "service", serviceName, "status", String.valueOf(statusCode)).increment();

        // 按状态码分类
        if (statusCode >= 200 && statusCode < 300) {
            meterRegistry.counter("gateway.status_codes.2xx", "service", serviceName).increment();
        } else if (statusCode >= 300 && statusCode < 400) {
            meterRegistry.counter("gateway.status_codes.3xx", "service", serviceName).increment();
        } else if (statusCode >= 400 && statusCode < 500) {
            meterRegistry.counter("gateway.status_codes.4xx", "service", serviceName).increment();
        } else if (statusCode >= 500) {
            meterRegistry.counter("gateway.status_codes.5xx", "service", serviceName).increment();
        }
    }

    // 获取监控数据的方法
    public Map<String, Object> getMonitoringData() {
        Map<String, Object> data = new HashMap<>();

        // 请求统计
        Map<String, Long> requestStats = new HashMap<>();
        requestCounts.forEach((service, count) -> requestStats.put(service, count.get()));
        data.put("requestCounts", requestStats);

        // 错误统计
        Map<String, Long> errorStats = new HashMap<>();
        errorCounts.forEach((service, count) -> errorStats.put(service, count.get()));
        data.put("errorCounts", errorStats);

        // 计算错误率
        Map<String, Double> errorRates = new HashMap<>();
        SERVICES.forEach(service -> {
            long totalRequests = requestCounts.getOrDefault(service, new AtomicLong(0)).get();
            long totalErrors = errorCounts.getOrDefault(service, new AtomicLong(0)).get();
            double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests : 0.0;
            errorRates.put(service, errorRate);
        });
        data.put("errorRates", errorRates);

        return data;
    }

    @Override
    public int getOrder() {
        return -120; // 在日志过滤器之后执行
    }
}