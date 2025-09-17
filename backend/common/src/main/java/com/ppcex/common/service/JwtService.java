package com.ppcex.common.service;

import com.ppcex.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * JWT服务类
 * 当配置了cex.jwt.enabled=true时才加载
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "cex.jwt.enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "cex.jwt")
public class JwtService {

    private String secret = "your-jwt-secret-key-at-least-32-bytes-long-for-security";
    private long expiration = 86400000; // 24小时，单位毫秒
    private long refreshExpiration = 604800000; // 7天，单位毫秒
    private String issuer = "PPCEX";

    /**
     * 创建用户Token
     */
    public String createUserToken(Long userId, String username, String... roles) {
        return JwtUtil.createUserToken(userId, username, secret, expiration, roles);
    }

    /**
     * 创建刷新Token
     */
    public String createRefreshToken(Long userId) {
        return JwtUtil.createRefreshToken(userId, secret, refreshExpiration);
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token, secret);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return JwtUtil.getUsernameFromToken(token, secret);
    }

    /**
     * 从Token中获取指定声明
     */
    public <T> T getClaimFromToken(String token, String claimName) {
        return JwtUtil.getClaimFromToken(token, claimName, secret);
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return JwtUtil.parseToken(token, secret);
    }

    /**
     * 获取Token剩余有效期
     */
    public long getTokenRemainingTime(String token) {
        return JwtUtil.getTokenRemainingTime(token, secret);
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        return JwtUtil.refreshToken(token, secret, expiration);
    }

    /**
     * 获取Token类型
     */
    public String getTypeFromToken(String token) {
        return JwtUtil.getTypeFromToken(token, secret);
    }

    /**
     * 验证用户Token
     */
    public boolean validateUserToken(String token, Long userId) {
        return JwtUtil.validateUserToken(token, userId, secret);
    }

    // Getter and Setter methods
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}