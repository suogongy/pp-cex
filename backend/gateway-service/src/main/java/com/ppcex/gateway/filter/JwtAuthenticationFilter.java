package com.ppcex.gateway.filter;

import com.ppcex.gateway.config.GatewayConfig;
import com.ppcex.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayConfig gatewayConfig;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String AUTH_CACHE_PREFIX = "auth:cache:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 检查是否是免认证路径
        if (isPermitAllPath(path)) {
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return handleError(exchange.getResponse(), 401, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 检查token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return handleError(exchange.getResponse(), 401, "Token has been revoked");
        }

        // 验证JWT token
        if (!jwtUtil.validateToken(token)) {
            return handleError(exchange.getResponse(), 401, "Invalid token");
        }

        // 检查token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            return handleError(exchange.getResponse(), 401, "Token has expired");
        }

        // 检查IP白名单
        if (!isIpAllowed(request)) {
            return handleError(exchange.getResponse(), 403, "IP not allowed");
        }

        // 获取用户信息
        String username = jwtUtil.getUsername(token);
        String userId = jwtUtil.getUserId(token);

        // 检查用户状态（从缓存中获取）
        if (!isUserActive(userId)) {
            return handleError(exchange.getResponse(), 401, "User account is disabled");
        }

        // 缓存认证结果
        cacheAuthResult(token, userId, username);

        // 在请求头中添加用户信息
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Name", username)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        log.debug("Authentication successful for user: {} ({})", username, userId);
        return chain.filter(mutatedExchange);
    }

    private boolean isPermitAllPath(String path) {
        return Arrays.stream(gatewayConfig.getSecurity().getPermitAll())
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private boolean isIpAllowed(ServerHttpRequest request) {
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        return Arrays.stream(gatewayConfig.getSecurity().getIpWhitelist())
                .anyMatch(whitelist -> {
                    if (whitelist.contains("/")) {
                        // CIDR格式
                        String[] parts = whitelist.split("/");
                        String ip = parts[0];
                        int prefix = Integer.parseInt(parts[1]);
                        return isIpInCidr(clientIp, ip, prefix);
                    } else {
                        return clientIp.equals(whitelist);
                    }
                });
    }

    private boolean isIpInCidr(String ip, String cidrIp, int prefix) {
        // 简化的CIDR检查，实际应该使用网络地址库
        return ip.startsWith(cidrIp.substring(0, Math.min(cidrIp.length(), prefix / 8 + 1)));
    }

    private boolean isUserActive(String userId) {
        String key = AUTH_CACHE_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return Boolean.TRUE.equals(cached);
        }

        // 如果缓存中没有，假设用户是活跃的（简化处理）
        // 实际应用中应该调用用户服务验证用户状态
        return true;
    }

    private void cacheAuthResult(String token, String userId, String username) {
        String key = AUTH_CACHE_PREFIX + token;
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);

        redisTemplate.opsForValue().set(key, userInfo,
                gatewayConfig.getCache().getAuthTtl(), TimeUnit.SECONDS);
    }

    private Mono<Void> handleError(ServerHttpResponse response, int statusCode, String message) {
        response.setStatusCode(HttpStatus.valueOf(statusCode));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        String responseBody = com.alibaba.fastjson2.JSON.toJSONString(errorResponse);
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());

        log.warn("Authentication failed: {} - {}", statusCode, message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，在认证相关的过滤器中最早执行
    }
}