package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 安全过滤器
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;

    /** SQL注入模式 */
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)(\\b(select|insert|update|delete|drop|alter|truncate|exec|execute)\\b)"),
        Pattern.compile("(?i)(\\b(union|join|where|having|group by|order by)\\b)"),
        Pattern.compile("(?i)(\\b(and|or|not|exists|between|like|in)\\b)"),
        Pattern.compile("('.+--|--)|(/\\*.*\\*/)|(\\|)|(;)")
    );

    /** XSS攻击模式 */
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
        Pattern.compile("<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE)
    );

    /** IP白名单 */
    private static final List<String> IP_WHITELIST = Arrays.asList(
        "127.0.0.1",
        "192.168.1.0/24"
    );

    /** IP黑名单 */
    private static final List<String> IP_BLACKLIST = Arrays.asList(
        "10.0.0.0/8"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);

        log.debug("Security filter processing request: {} from {}", path, clientIp);

        // IP白名单检查
        if (!isIpWhitelisted(clientIp)) {
            log.warn("IP {} not in whitelist for path: {}", clientIp, path);
            return forbidden(exchange.getResponse(), "IP地址不在白名单中");
        }

        // IP黑名单检查
        if (isIpBlacklisted(clientIp)) {
            log.warn("IP {} in blacklist for path: {}", clientIp, path);
            return forbidden(exchange.getResponse(), "IP地址已被封禁");
        }

        // 请求方法检查
        if (!isMethodAllowed(request.getMethod().name())) {
            log.warn("Method {} not allowed for path: {}", request.getMethod().name(), path);
            return methodNotAllowed(exchange.getResponse());
        }

        // User-Agent检查
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        if (isSuspiciousUserAgent(userAgent)) {
            log.warn("Suspicious User-Agent: {} for path: {}", userAgent, path);
            return badRequest(exchange.getResponse(), "可疑的请求来源");
        }

        // 参数安全检查
        if (hasMaliciousParameters(request)) {
            log.warn("Malicious parameters detected for path: {}", path);
            return badRequest(exchange.getResponse(), "请求参数包含恶意内容");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -80; // 在限流过滤器之后
    }

    /**
     * 检查IP是否在白名单中
     */
    private boolean isIpWhitelisted(String ip) {
        if (IP_WHITELIST.isEmpty()) {
            return true; // 如果白名单为空，允许所有IP
        }

        return IP_WHITELIST.stream().anyMatch(pattern ->
            pattern.contains("/") ? isIpInRange(ip, pattern) : pattern.equals(ip)
        );
    }

    /**
     * 检查IP是否在黑名单中
     */
    private boolean isIpBlacklisted(String ip) {
        return IP_BLACKLIST.stream().anyMatch(pattern ->
            pattern.contains("/") ? isIpInRange(ip, pattern) : pattern.equals(ip)
        );
    }

    /**
     * 检查IP是否在CIDR范围内
     */
    private boolean isIpInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // 简单的IP范围检查（实际实现需要更复杂的逻辑）
            return ip.startsWith(network.substring(0, Math.min(network.length(), prefixLength / 8 + 1)));
        } catch (Exception e) {
            log.error("IP range check failed for: {}", cidr, e);
            return false;
        }
    }

    /**
     * 检查请求方法是否允许
     */
    private boolean isMethodAllowed(String method) {
        return Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD").contains(method);
    }

    /**
     * 检查User-Agent是否可疑
     */
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return true; // 空User-Agent可能可疑
        }

        String lowerUserAgent = userAgent.toLowerCase();
        return lowerUserAgent.contains("bot") ||
               lowerUserAgent.contains("crawler") ||
               lowerUserAgent.contains("spider") ||
               lowerUserAgent.contains("scanner");
    }

    /**
     * 检查参数是否包含恶意内容
     */
    private boolean hasMaliciousParameters(ServerHttpRequest request) {
        // 检查查询参数
        request.getQueryParams().forEach((key, values) -> {
            for (String value : values) {
                if (isSqlInjection(value) || isXssAttack(value)) {
                    log.warn("Malicious parameter detected: {}={}", key, value);
                }
            }
        });

        // 检查路径变量
        String path = request.getPath().value();
        if (isSqlInjection(path) || isXssAttack(path)) {
            log.warn("Malicious path detected: {}", path);
            return true;
        }

        return false;
    }

    /**
     * 检查SQL注入
     */
    private boolean isSqlInjection(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }

        return SQL_INJECTION_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(input).find());
    }

    /**
     * 检查XSS攻击
     */
    private boolean isXssAttack(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }

        return XSS_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(input).find());
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

    /**
     * 返回方法不允许响应
     */
    private Mono<Void> methodNotAllowed(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 405);
        body.put("message", "请求方法不被允许");
        body.put("timestamp", System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(
            JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 返回错误请求响应
     */
    private Mono<Void> badRequest(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 400);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(
            JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}