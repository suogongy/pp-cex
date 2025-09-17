package com.ppcex.user.service;

import com.ppcex.common.util.JwtUtil;
import com.ppcex.user.config.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * JWT服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * 创建用户Token
     */
    public String createUserToken(Long userId, String username, String... roles) {
        return JwtUtil.createUserToken(userId, username, jwtProperties.getSecret(), jwtProperties.getExpiration(), roles);
    }

    /**
     * 创建刷新Token
     */
    public String createRefreshToken(Long userId) {
        return JwtUtil.createRefreshToken(userId, jwtProperties.getSecret(), jwtProperties.getRefreshExpiration());
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token, jwtProperties.getSecret());
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return JwtUtil.getUsernameFromToken(token, jwtProperties.getSecret());
    }

    /**
     * 从Token中获取指定声明
     */
    public <T> T getClaimFromToken(String token, String claimName) {
        return JwtUtil.getClaimFromToken(token, claimName, jwtProperties.getSecret());
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return JwtUtil.parseToken(token, jwtProperties.getSecret());
    }

    /**
     * 获取Token剩余有效期
     */
    public long getTokenRemainingTime(String token) {
        return JwtUtil.getTokenRemainingTime(token, jwtProperties.getSecret());
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        return JwtUtil.refreshToken(token, jwtProperties.getSecret(), jwtProperties.getExpiration());
    }

    /**
     * 获取Token类型
     */
    public String getTypeFromToken(String token) {
        return JwtUtil.getTypeFromToken(token, jwtProperties.getSecret());
    }

    /**
     * 验证用户Token
     */
    public boolean validateUserToken(String token, Long userId) {
        return JwtUtil.validateUserToken(token, userId, jwtProperties.getSecret());
    }
}