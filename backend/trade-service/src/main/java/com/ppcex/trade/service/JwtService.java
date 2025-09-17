package com.ppcex.trade.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        log.info("JwtService 初始化完成");
    }

    /**
     * 从token中提取指定claim（按名称）
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.get(claimName);
        } catch (Exception e) {
            log.warn("从token中提取claim失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中提取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("从token中提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析token获取所有claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT token");
        } catch (MalformedJwtException e) {
            log.warn("JWT token格式错误");
        } catch (SecurityException e) {
            log.warn("JWT token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token参数错误");
        }
        return false;
    }
}