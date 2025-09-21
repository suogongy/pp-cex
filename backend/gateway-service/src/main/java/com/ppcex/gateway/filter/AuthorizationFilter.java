package com.ppcex.gateway.filter;

import com.alibaba.fastjson2.JSON;
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
import org.springframework.web.server.ServerWebExchange;

import com.ppcex.gateway.config.GatewayConfig;

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

    private final StringRedisTemplate redisTemplate;
    private final GatewayConfig gatewayConfig;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final String PERMISSION_CACHE_PREFIX = "permission:user:";
    private static final String ROLE_PERMISSIONS_PREFIX = "role:permissions:";

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
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("开始权限授权检查 - 路径: {} {} - 客户端IP: {}", method, path, clientIp);

        // 检查是否是免权限路径
        if (isPermitAllPath(path)) {
            log.info("路径在免权限列表中，跳过权限检查 - 路径: {}", path);
            return chain.filter(exchange);
        }

        // 获取用户ID
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId == null) {
            log.warn("请求头中未找到用户ID - 路径: {} - 客户端IP: {}", path, clientIp);
            return handleError(exchange.getResponse(), 401, "User ID not found in request headers");
        }
        log.info("获取用户ID成功 - 用户ID: {}", userId);

        // 获取用户角色
        List<String> userRoles = getUserRoles(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("用户未分配角色 - 用户ID: {} - 路径: {}", userId, path);
            return handleError(exchange.getResponse(), 403, "User has no roles assigned");
        }
        log.info("获取用户角色成功 - 用户ID: {} 角色: {}", userId, userRoles);

        // 检查权限
        if (!hasPermission(path, method, userId, userRoles)) {
            log.warn("权限检查失败 - 用户ID: {} 路径: {} {} 角色: {}",
                    userId, method, path, userRoles);
            return handleError(exchange.getResponse(), 403, "Permission denied");
        }

        log.info("权限授权成功 - 用户ID: {} 路径: {} {} 客户端IP: {}",
                userId, method, path, clientIp);
        return chain.filter(exchange);
    }

    private boolean isPermitAllPath(String path) {
        boolean isPermitAll = Arrays.asList(gatewayConfig.getSecurity().getPermitAll()) .stream()
                .anyMatch(pattern -> {
                    boolean matches = antPathMatcher.match(pattern, path);
                    log.debug("免权限路径检查 - 路径: {} 模式: {} 匹配: {}", path, pattern, matches);
                    return matches;
                });
        log.debug("免权限路径检查结果 - 路径: {} 是否免权限: {}", path, isPermitAll);
        return isPermitAll;
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(String userId) {
        String key = "user:roles:" + userId;
        String roles = redisTemplate.opsForValue().get(key);

        if (roles != null) {
            List<String> roleList = JSON.parseArray(roles, String.class);
            log.debug("用户角色缓存命中 - 用户ID: {} 角色: {}", userId, roleList);
            return roleList;
        } else {
            log.debug("用户角色缓存未命中 - 用户ID: {}", userId);
            return null;
        }
    }

    private boolean hasPermission(String path, String method, String userId, List<String> userRoles) {
        log.debug("开始权限检查 - 用户ID: {} 路径: {} {} 角色: {}",
                userId, method, path, userRoles);

        // 1. 检查用户个人权限
        String userPermissionKey = PERMISSION_CACHE_PREFIX + userId;
        String userPermissionsStr = redisTemplate.opsForValue().get(userPermissionKey);
        List<String> userPermissions = userPermissionsStr != null ? JSON.parseArray(userPermissionsStr, String.class) : null;

        if (userPermissions != null) {
            log.debug("用户个人权限缓存命中 - 用户ID: {} 权限: {}", userId, userPermissions);
            if (checkApiPermission(path, method, userPermissions)) {
                log.debug("用户个人权限检查通过 - 用户ID: {}", userId);
                return true;
            }
        } else {
            log.debug("用户个人权限缓存未命中 - 用户ID: {}", userId);
        }

        // 2. 检查角色权限
        for (String role : userRoles) {
            String rolePermissionKey = ROLE_PERMISSIONS_PREFIX + role;
            String rolePermissionsStr = redisTemplate.opsForValue().get(rolePermissionKey);
            List<String> rolePermissions = rolePermissionsStr != null ? JSON.parseArray(rolePermissionsStr, String.class) : null;

            if (rolePermissions != null) {
                log.debug("角色权限缓存命中 - 角色: {} 权限: {}", role, rolePermissions);
                if (checkApiPermission(path, method, rolePermissions)) {
                    log.debug("角色权限检查通过 - 角色: {} 用户ID: {}", role, userId);
                    return true;
                }
            } else {
                log.debug("角色权限缓存未命中 - 角色: {}", role);
            }
        }

        log.debug("权限检查失败 - 用户ID: {} 路径: {} {}", userId, method, path);
        return false;
    }

    private boolean checkApiPermission(String path, String method, List<String> permissions) {
        log.debug("检查API权限 - 路径: {} 方法: {} 用户权限: {}",
                path, method, permissions);

        // 简化处理：检查路径权限
        String requiredPermission = API_PERMISSION_MAP.get(path);
        if (requiredPermission != null) {
            boolean hasPermission = permissions.contains(requiredPermission);
            log.debug("精确权限匹配 - 路径: {} 所需权限: {} 是否拥有: {}",
                    path, requiredPermission, hasPermission);
            return hasPermission;
        }

        // 模糊匹配权限
        for (Map.Entry<String, String> entry : API_PERMISSION_MAP.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), path)) {
                boolean hasPermission = permissions.contains(entry.getValue());
                log.debug("模糊权限匹配 - 路径: {} 模式: {} 所需权限: {} 是否拥有: {}",
                        path, entry.getKey(), entry.getValue(), hasPermission);
                return hasPermission;
            }
        }

        // 如果没有具体配置权限，默认允许（实际生产环境应该更严格）
        log.warn("路径未配置权限，默认允许 - 路径: {}", path);
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

        log.warn("权限授权失败 - 状态码: {} 错误信息: {}", statusCode, message);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90; // 在JWT认证过滤器之后执行
    }
}