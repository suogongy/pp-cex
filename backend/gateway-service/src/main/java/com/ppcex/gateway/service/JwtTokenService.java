package com.ppcex.gateway.service;

import com.alibaba.fastjson2.JSON;
import com.ppcex.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String TOKEN_REFRESH_PREFIX = "token:refresh:";
    private static final String USER_SESSION_PREFIX = "user:session:";

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String username, String userId) {
        log.info("开始生成访问令牌 - 用户: {} ({})", username, userId);

        String token = jwtUtil.generateToken(username, userId);
        log.info("JWT令牌生成成功 - 长度: {}", token.length());

        // 缓存用户会话信息
        cacheUserSession(userId, username, token);
        log.info("用户会话缓存完成 - 用户ID: {}", userId);

        log.info("访问令牌生成成功 - 用户: {} ({})", username, userId);
        return token;
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username, String userId) {
        log.info("开始生成刷新令牌 - 用户: {} ({})", username, userId);

        String refreshToken = jwtUtil.generateRefreshToken(username, userId);
        log.info("刷新JWT令牌生成成功 - 长度: {}", refreshToken.length());

        // 缓存刷新令牌
        String refreshKey = TOKEN_REFRESH_PREFIX + refreshToken;
        Map<String, Object> refreshInfo = new HashMap<>();
        refreshInfo.put("userId", userId);
        refreshInfo.put("username", username);
        refreshInfo.put("createdAt", System.currentTimeMillis());

        redisTemplate.opsForValue().set(refreshKey, JSON.toJSONString(refreshInfo), 7, TimeUnit.DAYS);
        log.info("刷新令牌缓存完成 - Key: {}", refreshKey.substring(0, Math.min(refreshKey.length(), 50)) + "...");

        log.info("刷新令牌生成成功 - 用户: {} ({})", username, userId);
        return refreshToken;
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("访问令牌为空");
            return false;
        }

        String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
        log.info("开始验证访问令牌 - Token: {}", tokenPreview);

        // 检查是否在黑名单中
        if (isTokenBlacklisted(token)) {
            log.warn("访问令牌在黑名单中 - Token: {}", tokenPreview);
            return false;
        }
        log.info("访问令牌黑名单检查通过");

        // 验证JWT格式
        if (!jwtUtil.validateToken(token)) {
            log.warn("访问令牌JWT格式无效 - Token: {}", tokenPreview);
            return false;
        }
        log.info("访问令牌JWT格式验证通过");

        // 检查是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("访问令牌已过期 - Token: {}", tokenPreview);
            return false;
        }
        log.info("访问令牌过期时间检查通过");

        // 检查是否是刷新令牌
        if (jwtUtil.isRefreshToken(token)) {
            log.warn("刷新令牌被用作访问令牌 - Token: {}", tokenPreview);
            return false;
        }
        log.info("访问令牌类型检查通过");

        // 检查用户会话是否有效
        String userId = jwtUtil.getUserId(token);
        if (!isUserSessionValid(userId, token)) {
            log.warn("用户会话无效 - 用户ID: {}", userId);
            return false;
        }
        log.info("用户会话验证通过 - 用户ID: {}", userId);

        log.info("访问令牌验证成功 - 用户ID: {}", userId);
        return true;
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.warn("刷新令牌为空");
            return false;
        }

        String tokenPreview = refreshToken.length() > 10 ? refreshToken.substring(0, 10) + "..." : refreshToken;
        log.info("开始验证刷新令牌 - Token: {}", tokenPreview);

        // 检查是否在黑名单中
        if (isTokenBlacklisted(refreshToken)) {
            log.warn("刷新令牌在黑名单中 - Token: {}", tokenPreview);
            return false;
        }
        log.info("刷新令牌黑名单检查通过");

        // 验证JWT格式
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("刷新令牌JWT格式无效 - Token: {}", tokenPreview);
            return false;
        }
        log.info("刷新令牌JWT格式验证通过");

        // 检查是否过期
        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.warn("刷新令牌已过期 - Token: {}", tokenPreview);
            return false;
        }
        log.info("刷新令牌过期时间检查通过");

        // 检查是否是刷新令牌
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("访问令牌被用作刷新令牌 - Token: {}", tokenPreview);
            return false;
        }
        log.info("刷新令牌类型检查通过");

        // 检查刷新令牌是否在缓存中
        String refreshKey = TOKEN_REFRESH_PREFIX + refreshToken;
        String cached = redisTemplate.opsForValue().get(refreshKey);
        if (cached == null) {
            log.warn("刷新令牌不在缓存中 - Key: {}", refreshKey.substring(0, Math.min(refreshKey.length(), 50)) + "...");
            return false;
        }

        // 解析JSON字符串为Map
        Map<String, Object> refreshInfo = JSON.parseObject(cached, Map.class);
        log.info("刷新令牌缓存验证通过");

        log.info("刷新令牌验证成功");
        return true;
    }

    /**
     * 刷新访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        log.info("开始刷新访问令牌");

        if (!validateRefreshToken(refreshToken)) {
            log.error("刷新令牌验证失败，无法刷新访问令牌");
            throw new IllegalArgumentException("Invalid refresh token");
        }
        log.info("刷新令牌验证通过");

        String userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        log.info("获取用户信息成功 - 用户: {} ({})", username, userId);

        // 将旧的刷新令牌加入黑名单
        blacklistToken(refreshToken);
        log.info("旧刷新令牌已加入黑名单");

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = generateAccessToken(username, userId);
        String newRefreshToken = generateRefreshToken(username, userId);
        log.info("新令牌生成完成");

        log.info("访问令牌刷新成功 - 用户: {} ({})", username, userId);
        return newAccessToken;
    }

    /**
     * 撤销令牌
     */
    public void revokeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("撤销令牌为空");
            return;
        }

        String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
        log.info("开始撤销令牌 - Token: {}", tokenPreview);

        // 检查令牌类型
        String userId = jwtUtil.getUserId(token);
        if (userId != null) {
            log.info("清除用户会话 - 用户ID: {}", userId);
            // 清除用户会话
            clearUserSession(userId);
        }

        // 加入黑名单
        blacklistToken(token);
        log.info("令牌已加入黑名单");

        // 如果是刷新令牌，清除刷新令牌缓存
        if (jwtUtil.isRefreshToken(token)) {
            String refreshKey = TOKEN_REFRESH_PREFIX + token;
            redisTemplate.delete(refreshKey);
            log.info("刷新令牌缓存已清除 - Key: {}", refreshKey.substring(0, Math.min(refreshKey.length(), 50)) + "...");
        }

        log.info("令牌撤销完成 - 用户ID: {}", userId);
    }

    /**
     * 获取令牌信息
     */
    public Map<String, Object> getTokenInfo(String token) {
        log.info("开始获取令牌信息");

        Map<String, Object> info = new HashMap<>();

        if (!validateAccessToken(token)) {
            log.warn("令牌验证失败，返回无效信息");
            info.put("valid", false);
            return info;
        }

        String userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        info.put("valid", true);
        info.put("userId", userId);
        info.put("username", username);
        info.put("issuedAt", jwtUtil.getExpirationDate(token));
        info.put("expiresAt", jwtUtil.getExpirationDate(token));

        log.info("令牌信息获取成功 - 用户: {} ({})", username, userId);
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
        String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
        log.info("将令牌加入黑名单 - Token: {}", tokenPreview);

        String key = TOKEN_BLACKLIST_PREFIX + token;

        // 计算令牌剩余有效期
        long expiration = jwtUtil.getExpirationDate(token).getTime();
        long ttl = expiration - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            log.info("令牌加入黑名单，TTL: {}ms - Key: {}", ttl, key.substring(0, Math.min(key.length(), 50)) + "...");
        } else {
            redisTemplate.opsForValue().set(key, "blacklisted", 1, TimeUnit.HOURS);
            log.info("令牌已过期，加入黑名单1小时 - Key: {}", key.substring(0, Math.min(key.length(), 50)) + "...");
        }
    }

    /**
     * 缓存用户会话
     */
    private void cacheUserSession(String userId, String username, String token) {
        log.info("开始缓存用户会话 - 用户: {} ({})", username, userId);

        String sessionKey = USER_SESSION_PREFIX + userId;
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("userId", userId);
        sessionInfo.put("username", username);
        sessionInfo.put("accessToken", token);
        sessionInfo.put("lastActivity", System.currentTimeMillis());

        redisTemplate.opsForValue().set(sessionKey, JSON.toJSONString(sessionInfo), 1, TimeUnit.HOURS);
        log.info("用户会话缓存成功 - Key: {}", sessionKey);
    }

    /**
     * 检查用户会话是否有效
     */
    private boolean isUserSessionValid(String userId, String token) {
        log.info("检查用户会话有效性 - 用户ID: {}", userId);

        String sessionKey = USER_SESSION_PREFIX + userId;
        String session = redisTemplate.opsForValue().get(sessionKey);

        if (session != null) {
            Map<String, Object> sessionInfo = JSON.parseObject(session, Map.class);
            String cachedToken = (String) sessionInfo.get("accessToken");

            // 检查令牌是否匹配
            if (token.equals(cachedToken)) {
                // 更新最后活动时间
                sessionInfo.put("lastActivity", System.currentTimeMillis());
                redisTemplate.opsForValue().set(sessionKey, JSON.toJSONString(sessionInfo), 1, TimeUnit.HOURS);
                log.info("用户会话有效且已更新活动时间 - 用户ID: {}", userId);
                return true;
            } else {
                log.warn("用户会话令牌不匹配 - 用户ID: {}", userId);
            }
        } else {
            log.warn("用户会话不存在或格式错误 - 用户ID: {}", userId);
        }

        return false;
    }

    /**
     * 清除用户会话
     */
    private void clearUserSession(String userId) {
        log.info("清除用户会话 - 用户ID: {}", userId);

        String sessionKey = USER_SESSION_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(sessionKey);
        log.info("用户会话清除结果: {} - Key: {}", deleted ? "成功" : "失败", sessionKey);
    }

    /**
     * 获取用户会话信息
     */
    public Map<String, Object> getUserSession(String userId) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        String session = redisTemplate.opsForValue().get(sessionKey);

        if (session != null) {
            return JSON.parseObject(session, Map.class);
        }

        return null;
    }

    /**
     * 清理过期的会话
     */
    public void cleanupExpiredSessions() {
        log.info("开始清理过期会话");

        int cleanedCount = 0;

        // 清理过期的刷新令牌
        Set<String> refreshKeys = redisTemplate.keys(TOKEN_REFRESH_PREFIX + "*");
        if (refreshKeys != null && !refreshKeys.isEmpty()) {
            log.info("发现 {} 个刷新令牌需要检查", refreshKeys.size());

            for (String key : refreshKeys) {
                String session = redisTemplate.opsForValue().get(key);
                if (session != null) {
                    Map<String, Object> refreshInfo = JSON.parseObject(session, Map.class);
                    long createdAt = Long.parseLong(refreshInfo.get("createdAt").toString());
                    if (System.currentTimeMillis() - createdAt > 7 * 24 * 60 * 60 * 1000L) { // 7天
                        redisTemplate.delete(key);
                        cleanedCount++;
                        log.debug("已清理过期刷新令牌 - Key: {}", key.substring(0, Math.min(key.length(), 50)) + "...");
                    }
                }
            }
        }

        log.info("过期会话清理完成，共清理 {} 个", cleanedCount);
    }
}