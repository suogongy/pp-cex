package com.ppcex.gateway.service;

import com.ppcex.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String TOKEN_REFRESH_PREFIX = "token:refresh:";
    private static final String USER_SESSION_PREFIX = "user:session:";

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String username, String userId) {
        String token = jwtUtil.generateToken(username, userId);

        // 缓存用户会话信息
        cacheUserSession(userId, username, token);

        log.info("Generated access token for user: {} ({})", username, userId);
        return token;
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username, String userId) {
        String refreshToken = jwtUtil.generateRefreshToken(username, userId);

        // 缓存刷新令牌
        String refreshKey = TOKEN_REFRESH_PREFIX + refreshToken;
        Map<String, Object> refreshInfo = new HashMap<>();
        refreshInfo.put("userId", userId);
        refreshInfo.put("username", username);
        refreshInfo.put("createdAt", System.currentTimeMillis());

        redisTemplate.opsForValue().set(refreshKey, refreshInfo, 7, TimeUnit.DAYS);

        log.info("Generated refresh token for user: {} ({})", username, userId);
        return refreshToken;
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // 检查是否在黑名单中
        if (isTokenBlacklisted(token)) {
            log.warn("Token is blacklisted: {}", token.substring(0, 10) + "...");
            return false;
        }

        // 验证JWT格式
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token: {}", token.substring(0, 10) + "...");
            return false;
        }

        // 检查是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token has expired: {}", token.substring(0, 10) + "...");
            return false;
        }

        // 检查是否是刷新令牌
        if (jwtUtil.isRefreshToken(token)) {
            log.warn("Refresh token used as access token: {}", token.substring(0, 10) + "...");
            return false;
        }

        // 检查用户会话是否有效
        String userId = jwtUtil.getUserId(token);
        if (!isUserSessionValid(userId, token)) {
            log.warn("User session invalid for user: {}", userId);
            return false;
        }

        return true;
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return false;
        }

        // 检查是否在黑名单中
        if (isTokenBlacklisted(refreshToken)) {
            return false;
        }

        // 验证JWT格式
        if (!jwtUtil.validateToken(refreshToken)) {
            return false;
        }

        // 检查是否过期
        if (jwtUtil.isTokenExpired(refreshToken)) {
            return false;
        }

        // 检查是否是刷新令牌
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            return false;
        }

        // 检查刷新令牌是否在缓存中
        String refreshKey = TOKEN_REFRESH_PREFIX + refreshToken;
        Object cached = redisTemplate.opsForValue().get(refreshKey);
        if (cached == null) {
            return false;
        }

        return true;
    }

    /**
     * 刷新访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);

        // 将旧的刷新令牌加入黑名单
        blacklistToken(refreshToken);

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = generateAccessToken(username, userId);
        String newRefreshToken = generateRefreshToken(username, userId);

        log.info("Refreshed access token for user: {} ({})", username, userId);
        return newAccessToken;
    }

    /**
     * 撤销令牌
     */
    public void revokeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        // 检查令牌类型
        String userId = jwtUtil.getUserId(token);
        if (userId != null) {
            // 清除用户会话
            clearUserSession(userId);
        }

        // 加入黑名单
        blacklistToken(token);

        // 如果是刷新令牌，清除刷新令牌缓存
        if (jwtUtil.isRefreshToken(token)) {
            String refreshKey = TOKEN_REFRESH_PREFIX + token;
            redisTemplate.delete(refreshKey);
        }

        log.info("Revoked token for user: {}", userId);
    }

    /**
     * 获取令牌信息
     */
    public Map<String, Object> getTokenInfo(String token) {
        Map<String, Object> info = new HashMap<>();

        if (!validateAccessToken(token)) {
            info.put("valid", false);
            return info;
        }

        info.put("valid", true);
        info.put("userId", jwtUtil.getUserId(token));
        info.put("username", jwtUtil.getUsername(token));
        info.put("issuedAt", jwtUtil.getExpirationDate(token));
        info.put("expiresAt", jwtUtil.getExpirationDate(token));

        return info;
    }

    /**
     * 检查令牌是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 将令牌加入黑名单
     */
    private void blacklistToken(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;

        // 计算令牌剩余有效期
        long expiration = jwtUtil.getExpirationDate(token).getTime();
        long ttl = expiration - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, "blacklisted", 1, TimeUnit.HOURS);
        }

        log.debug("Token blacklisted: {}", token.substring(0, 10) + "...");
    }

    /**
     * 缓存用户会话
     */
    private void cacheUserSession(String userId, String username, String token) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("userId", userId);
        sessionInfo.put("username", username);
        sessionInfo.put("accessToken", token);
        sessionInfo.put("lastActivity", System.currentTimeMillis());

        redisTemplate.opsForValue().set(sessionKey, sessionInfo, 1, TimeUnit.HOURS);
    }

    /**
     * 检查用户会话是否有效
     */
    private boolean isUserSessionValid(String userId, String token) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        Object session = redisTemplate.opsForValue().get(sessionKey);

        if (session instanceof Map) {
            Map<String, Object> sessionInfo = (Map<String, Object>) session;
            String cachedToken = (String) sessionInfo.get("accessToken");

            // 检查令牌是否匹配
            if (token.equals(cachedToken)) {
                // 更新最后活动时间
                sessionInfo.put("lastActivity", System.currentTimeMillis());
                redisTemplate.opsForValue().set(sessionKey, sessionInfo, 1, TimeUnit.HOURS);
                return true;
            }
        }

        return false;
    }

    /**
     * 清除用户会话
     */
    private void clearUserSession(String userId) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        redisTemplate.delete(sessionKey);
    }

    /**
     * 获取用户会话信息
     */
    public Map<String, Object> getUserSession(String userId) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        Object session = redisTemplate.opsForValue().get(sessionKey);

        if (session instanceof Map) {
            return (Map<String, Object>) session;
        }

        return null;
    }

    /**
     * 清理过期的会话
     */
    public void cleanupExpiredSessions() {
        // 清理过期的刷新令牌
        Set<String> refreshKeys = redisTemplate.keys(TOKEN_REFRESH_PREFIX + "*");
        if (refreshKeys != null && !refreshKeys.isEmpty()) {
            for (String key : refreshKeys) {
                Object session = redisTemplate.opsForValue().get(key);
                if (session instanceof Map) {
                    Map<String, Object> refreshInfo = (Map<String, Object>) session;
                    long createdAt = (Long) refreshInfo.get("createdAt");
                    if (System.currentTimeMillis() - createdAt > 7 * 24 * 60 * 60 * 1000L) { // 7天
                        redisTemplate.delete(key);
                    }
                }
            }
        }

        log.info("Cleaned up expired sessions");
    }
}