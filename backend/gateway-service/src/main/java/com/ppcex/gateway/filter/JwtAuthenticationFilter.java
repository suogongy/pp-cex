package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.ppcex.gateway.config.GatewayConfig;
import com.ppcex.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;
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
        String method = request.getMethod().name();
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // 为Swagger相关请求添加短路逻辑，减少日志输出
        if (isSwaggerRequest(path)) {
            log.debug("Swagger文档请求，跳过JWT认证 - 路径: {} 方法: {}", path, method);
            return chain.filter(exchange);
        }

        log.info("开始JWT认证处理 - 路径: {} {} - 客户端IP: {}", method, path, clientIp);

        // 检查是否是免认证路径
        if (isPermitAllPath(path)) {
            log.info("路径在免认证列表中，跳过JWT认证 - 路径: {} 方法: {} 客户端IP: {}", path, method, clientIp);
            return chain.filter(exchange);
        } else {
            log.warn("路径不在免认证列表中，需要进行JWT认证 - 路径: {} 方法: {} 客户端IP: {}", path, method, clientIp);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        log.info("获取Authorization头: {}", authHeader != null ? "已获取" : "未获取");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Authorization头缺失或格式错误 - 客户端IP: {}", clientIp);
            return handleError(exchange.getResponse(), 401, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
        log.info("提取Token成功 - Token: {}", tokenPreview);

        // 检查token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            log.warn("Token在黑名单中 - Token: {} - 客户端IP: {}", tokenPreview, clientIp);
            return handleError(exchange.getResponse(), 401, "Token has been revoked");
        }
        log.info("Token黑名单检查通过");

        // 验证JWT token
        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT Token验证失败 - Token: {} - 客户端IP: {}", tokenPreview, clientIp);
            return handleError(exchange.getResponse(), 401, "Invalid token");
        }
        log.info("JWT Token验证通过");

        // 检查token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token已过期 - Token: {} - 客户端IP: {}", tokenPreview, clientIp);
            return handleError(exchange.getResponse(), 401, "Token has expired");
        }
        log.info("Token过期时间检查通过");

        // 检查IP白名单
        if (!isIpAllowed(request)) {
            log.warn("IP不在白名单中 - IP: {} - Token: {}", clientIp, tokenPreview);
            return handleError(exchange.getResponse(), 403, "IP not allowed");
        }
        log.info("IP白名单检查通过 - IP: {}", clientIp);

        // 获取用户信息
        String username = jwtUtil.getUsername(token);
        String userId = jwtUtil.getUserId(token);
        log.info("获取用户信息成功 - 用户: {} ({})", username, userId);

        // 检查用户状态（从缓存中获取）
        if (!isUserActive(userId)) {
            log.warn("用户账户已禁用 - 用户ID: {} - 客户端IP: {}", userId, clientIp);
            return handleError(exchange.getResponse(), 401, "User account is disabled");
        }
        log.info("用户状态检查通过 - 用户ID: {}", userId);

        // 缓存认证结果
        cacheAuthResult(token, userId, username);
        log.info("认证结果缓存完成 - 用户: {} ({})", username, userId);

        // 在请求头中添加用户信息
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Name", username)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        log.info("JWT认证成功 - 用户: {} ({}) - 路径: {} {} - 客户端IP: {}",
                username, userId, method, path, clientIp);
        return chain.filter(mutatedExchange);
    }

    private boolean isPermitAllPath(String path) {
        log.debug("开始检查免认证路径 - 路径: {}", path);

        String[] permitAllPaths = gatewayConfig.getSecurity().getPermitAll();
        log.debug("配置的免认证路径数量: {}", permitAllPaths != null ? permitAllPaths.length : 0);

        if (permitAllPaths != null) {
            for (String pattern : permitAllPaths) {
                boolean matches = antPathMatcher.match(pattern, path);
                log.debug("检查免认证路径 - 路径: {} 模式: {} 匹配: {}", path, pattern, matches);
                if (matches) {
                    log.info("路径匹配免认证模式 - 路径: {} 匹配模式: {}", path, pattern);
                    return true;
                }
            }
        }

        log.debug("路径未匹配任何免认证模式 - 路径: {}", path);
        return false;
    }

    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        log.debug("Token黑名单检查 - Key: {} 是否在黑名单: {}",
                key.substring(0, Math.min(key.length(), 50)) + "...", isBlacklisted);
        return isBlacklisted;
    }

    private boolean isIpAllowed(ServerHttpRequest request) {
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.debug("IP白名单检查 - 客户端IP: {}", clientIp);

        // 如果是Swagger相关请求，允许本地访问
        String path = request.getPath().value();
        if (isSwaggerRequest(path) && (clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1"))) {
            log.info("Swagger文档本地访问 - 路径: {} 客户端IP: {}, 允许访问", path, clientIp);
            return true;
        }

        String[] ipWhitelist = gatewayConfig.getSecurity().getIpWhitelist();
        if (ipWhitelist == null || ipWhitelist.length == 0) {
            log.debug("IP白名单未配置，默认允许访问 - 客户端IP: {}", clientIp);
            return true;
        }

        boolean isAllowed = Arrays.stream(ipWhitelist)
                .anyMatch(whitelist -> {
                    boolean matches;
                    if (whitelist.contains("/")) {
                        // CIDR格式
                        String[] parts = whitelist.split("/");
                        String ip = parts[0];
                        int prefix = Integer.parseInt(parts[1]);
                        matches = isIpInCidr(clientIp, ip, prefix);
                    } else {
                        matches = clientIp.equals(whitelist);
                    }
                    log.debug("IP白名单匹配检查 - 客户端IP: {} 白名单项: {} 匹配结果: {}",
                            clientIp, whitelist, matches);
                    return matches;
                });

        log.info("IP白名单检查结果 - 客户端IP: {} 是否允许: {}", clientIp, isAllowed);
        return isAllowed;
    }

    private boolean isSwaggerRequest(String path) {
        // 基础Swagger UI路径
        if (path.startsWith("/doc.html") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/swagger-resources/") ||
                path.startsWith("/webjars/") ||
                path.equals("/v3/api-docs/swagger-config") ||
                path.startsWith("/favicon.ico")) {
            return true;
        }

        // 各服务的OpenAPI文档路径 - 支持服务名称前缀
        if (path.startsWith("/v3/api-docs/") || path.equals("/v3/api-docs")) {
            return true;
        }

        // 为Knife4j发现的路径添加支持 - 匹配 /api/v1/{service}/v3/api-docs/**
        if (path.startsWith("/api/v1/user/v3/api-docs") ||
                path.startsWith("/api/v1/trade/v3/api-docs") ||
                path.startsWith("/api/v1/wallet/v3/api-docs") ||
                path.startsWith("/api/v1/finance/v3/api-docs") ||
                path.startsWith("/api/v1/market/v3/api-docs") ||
                path.startsWith("/api/v1/risk/v3/api-docs") ||
                path.startsWith("/api/v1/notify/v3/api-docs") ||
                path.startsWith("/api/v1/match/v3/api-docs")) {
            return true;
        }

        // 直接服务名称前缀的路径 - 匹配 /{service-name}/v3/api-docs/**
        if (path.startsWith("/user-service/v3/api-docs") ||
                path.startsWith("/trade-service/v3/api-docs") ||
                path.startsWith("/wallet-service/v3/api-docs") ||
                path.startsWith("/finance-service/v3/api-docs") ||
                path.startsWith("/market-service/v3/api-docs") ||
                path.startsWith("/risk-service/v3/api-docs") ||
                path.startsWith("/notify-service/v3/api-docs") ||
                path.startsWith("/match-service/v3/api-docs")) {
            return true;
        }

        // 特殊路径 - favicon.ico 等常被请求的资源
        if (path.equals("/favicon.ico")) {
            return true;
        }


        return false;
    }

    private boolean isIpInCidr(String ip, String cidrIp, int prefix) {
        // 简化的CIDR检查，实际应该使用网络地址库
        boolean inCidr = ip.startsWith(cidrIp.substring(0, Math.min(cidrIp.length(), prefix / 8 + 1)));
        log.debug("CIDR检查 - IP: {} CIDR: {}{} 前缀: {} 结果: {}",
                ip, cidrIp, "/" + prefix, prefix, inCidr);
        return inCidr;
    }

    private boolean isUserActive(String userId) {
        String key = AUTH_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            boolean isActive = Boolean.parseBoolean(cached);
            log.debug("用户状态缓存命中 - 用户ID: {} 状态: {}", userId, isActive ? "活跃" : "禁用");
            return isActive;
        }

        // 如果缓存中没有，假设用户是活跃的（简化处理）
        // 实际应用中应该调用用户服务验证用户状态
        log.debug("用户状态缓存未命中，默认为活跃 - 用户ID: {}", userId);
        return true;
    }

    private void cacheAuthResult(String token, String userId, String username) {
        String key = AUTH_CACHE_PREFIX + token;
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);

        redisTemplate.opsForValue().set(key, JSON.toJSONString(userInfo),
                gatewayConfig.getCache().getAuthTtl(), TimeUnit.SECONDS);
        log.info("认证结果缓存成功 - 用户: {} ({}) - TTL: {}秒 - Key: {}",
                username, userId, gatewayConfig.getCache().getAuthTtl(),
                key.substring(0, Math.min(key.length(), 50)) + "...");
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

        log.warn("JWT认证失败 - 状态码: {} 错误信息: {}", statusCode, message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 中等优先级，在CORS之后但在其他过滤器之前执行
    }
}