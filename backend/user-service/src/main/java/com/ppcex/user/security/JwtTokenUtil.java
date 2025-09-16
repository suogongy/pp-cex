package com.ppcex.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${cex.user.jwt.secret}")
    private String secret;

    @Value("${cex.user.jwt.expiration}")
    private Long expiration;

    @Value("${cex.user.jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从token中提取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析token获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT token签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token参数错误: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Map<String, Object> finalClaims = new HashMap<>(claims);
        finalClaims.put("sub", username);
        finalClaims.put("iat", new Date(System.currentTimeMillis()));
        finalClaims.put("exp", new Date(System.currentTimeMillis() + expiration));

        return doGenerateToken(finalClaims, username);
    }

    /**
     * 生成访问token
     */
    public String generateAccessToken(String username, Long userId, String userNo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userNo", userNo);
        claims.put("type", "access");
        return generateToken(username, claims);
    }

    /**
     * 生成刷新token
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成token的具体实现
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token（不需要用户名）
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从token中提取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中提取用户编号
     */
    public String getUserNoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userNo", String.class);
    }

    /**
     * 从token中提取token类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 检查是否为刷新token
     */
    public Boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为访问token
     */
    public Boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}