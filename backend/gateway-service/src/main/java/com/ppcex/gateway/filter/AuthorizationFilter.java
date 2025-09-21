package com.ppcex.gateway.filter;

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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final String PERMISSION_CACHE_PREFIX = "permission:user:";
    private static final String ROLE_PERMISSIONS_PREFIX = "role:permissions:";

    // 免权限检查的路径
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            "/api/v1/auth/**",
            "/actuator/**",
            "/api/v1/market/**",
            "/fallback/**"
    );

    // API权限映射
    private static final Map<String, String> API_PERMISSION_MAP = new HashMap<>();
    static {
        // 用户服务权限
        API_PERMISSION_MAP.put("/api/v1/user/profile", "user:profile:read");
        API_PERMISSION_MAP.put("/api/v1/user/profile/update", "user:profile:update");
        API_PERMISSION_MAP.put("/api/v1/user/password", "user:password:update");
        API_PERMISSION_MAP.put("/api/v1/user/kyc", "user:kyc:read");
        API_PERMISSION_MAP.put("/api/v1/user/kyc/submit", "user:kyc:create");

        // 交易服务权限
        API_PERMISSION_MAP.put("/api/v1/trade/order", "trade:order:create");
        API_PERMISSION_MAP.put("/api/v1/trade/order/list", "trade:order:read");
        API_PERMISSION_MAP.put("/api/v1/trade/order/cancel", "trade:order:cancel");
        API_PERMISSION_MAP.put("/api/v1/trade/pair", "trade:pair:read");
        API_PERMISSION_MAP.put("/api/v1/trade/history", "trade:history:read");

        // 钱包服务权限
        API_PERMISSION_MAP.put("/api/v1/wallet/balance", "wallet:balance:read");
        API_PERMISSION_MAP.put("/api/v1/wallet/deposit", "wallet:deposit:create");
        API_PERMISSION_MAP.put("/api/v1/wallet/withdraw", "wallet:withdraw:create");
        API_PERMISSION_MAP.put("/api/v1/wallet/transfer", "wallet:transfer:create");
        API_PERMISSION_MAP.put("/api/v1/wallet/address", "wallet:address:read");

        // 财务服务权限
        API_PERMISSION_MAP.put("/api/v1/finance/transaction", "finance:transaction:read");
        API_PERMISSION_MAP.put("/api/v1/finance/summary", "finance:summary:read");

        // 风控服务权限
        API_PERMISSION_MAP.put("/api/v1/risk/check", "risk:check:read");

        // 通知服务权限
        API_PERMISSION_MAP.put("/api/v1/notify/setting", "notify:setting:read");
        API_PERMISSION_MAP.put("/api/v1/notify/setting/update", "notify:setting:update");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // 检查是否是免权限路径
        if (isPermitAllPath(path)) {
            return chain.filter(exchange);
        }

        // 获取用户ID
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId == null) {
            return handleError(exchange.getResponse(), 401, "User ID not found in request headers");
        }

        // 获取用户角色
        List<String> userRoles = getUserRoles(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return handleError(exchange.getResponse(), 403, "User has no roles assigned");
        }

        // 检查权限
        if (!hasPermission(path, method, userId, userRoles)) {
            return handleError(exchange.getResponse(), 403, "Permission denied");
        }

        log.debug("Authorization successful for user: {}, path: {}", userId, path);
        return chain.filter(exchange);
    }

    private boolean isPermitAllPath(String path) {
        return PERMIT_ALL_PATHS.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(String userId) {
        String key = "user:roles:" + userId;
        Object roles = redisTemplate.opsForValue().get(key);
        return roles instanceof List ? (List<String>) roles : null;
    }

    private boolean hasPermission(String path, String method, String userId, List<String> userRoles) {
        // 1. 检查用户个人权限
        String userPermissionKey = PERMISSION_CACHE_PREFIX + userId;
        List<String> userPermissions = (List<String>) redisTemplate.opsForValue().get(userPermissionKey);

        if (userPermissions != null && checkApiPermission(path, method, userPermissions)) {
            return true;
        }

        // 2. 检查角色权限
        for (String role : userRoles) {
            String rolePermissionKey = ROLE_PERMISSIONS_PREFIX + role;
            List<String> rolePermissions = (List<String>) redisTemplate.opsForValue().get(rolePermissionKey);

            if (rolePermissions != null && checkApiPermission(path, method, rolePermissions)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkApiPermission(String path, String method, List<String> permissions) {
        // 简化处理：检查路径权限
        String requiredPermission = API_PERMISSION_MAP.get(path);
        if (requiredPermission != null) {
            return permissions.contains(requiredPermission);
        }

        // 模糊匹配权限
        for (Map.Entry<String, String> entry : API_PERMISSION_MAP.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), path)) {
                return permissions.contains(entry.getValue());
            }
        }

        // 如果没有具体配置权限，默认允许（实际生产环境应该更严格）
        log.warn("No permission configured for path: {}", path);
        return true;
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

        log.warn("Authorization failed: {} - {}", statusCode, message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90; // 在JWT认证过滤器之后执行
    }
}