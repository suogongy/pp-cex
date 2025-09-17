package com.ppcex.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {

    /**
     * 生成JWT Token
     *
     * @param subject 主题
     * @param claims  声明
     * @param secret  密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT Token
     */
    public static String generateToken(String subject, Map<String, Object> claims, String secret, long expiration) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(secret))
                .compact();
    }

    /**
     * 生成JWT Token
     *
     * @param subject 主题
     * @param secret  密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT Token
     */
    public static String generateToken(String subject, String secret, long expiration) {
        return generateToken(subject, new HashMap<>(), secret, expiration);
    }

    /**
     * 生成密钥
     */
    private static SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    
    /**
     * 解析JWT Token
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return Claims
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证JWT Token
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT Token安全错误: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token参数错误: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 用户名
     */
    public static String getUsernameFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getSubject, secret);
    }

    /**
     * 从Token中获取过期时间
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getExpiration, secret);
    }

    /**
     * 从Token中获取指定声明
     *
     * @param token          JWT Token
     * @param claimsResolver 声明解析器
     * @param secret         密钥
     * @param <T>            声明类型
     * @return 声明值
     */
    public static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, String secret) {
        final Claims claims = parseToken(token, secret);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中获取指定声明
     *
     * @param token     JWT Token
     * @param claimName 声明名称
     * @param secret    密钥
     * @param <T>       声明类型
     * @return 声明值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getClaimFromToken(String token, String claimName, String secret) {
        final Claims claims = parseToken(token, secret);
        return (T) claims.get(claimName);
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token, String secret) {
        final Date expiration = getExpirationDateFromToken(token, secret);
        return expiration.before(new Date());
    }

    /**
     * 刷新Token
     *
     * @param token JWT Token
     * @param secret 密钥
     * @param expiration 过期时间
     * @return 新的JWT Token
     */
    public static String refreshToken(String token, String secret, long expiration) {
        final Claims claims = parseToken(token, secret);
        return generateToken(claims.getSubject(), claims, secret, expiration);
    }

    /**
     * 获取Token剩余有效期（毫秒）
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 剩余有效期
     */
    public static long getTokenRemainingTime(String token, String secret) {
        final Date expiration = getExpirationDateFromToken(token, secret);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 检查Token是否即将过期（剩余时间小于5分钟）
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon(String token, String secret) {
        long remainingTime = getTokenRemainingTime(token, secret);
        return remainingTime > 0 && remainingTime < 5 * 60 * 1000;
    }

    /**
     * 获取Token签发时间
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 签发时间
     */
    public static Date getIssuedAtDateFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getIssuedAt, secret);
    }

    /**
     * 获取Token ID
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return Token ID
     */
    public static String getIdFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getId, secret);
    }

    /**
     * 获取Token签发者
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 签发者
     */
    public static String getIssuerFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getIssuer, secret);
    }

    /**
     * 获取Token受众
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return 受众集合
     */
    public static java.util.Set<String> getAudienceFromToken(String token, String secret) {
        return getClaimFromToken(token, Claims::getAudience, secret);
    }

    /**
     * 获取Token类型
     *
     * @param token JWT Token
     * @param secret 密钥
     * @return Token类型
     */
    public static String getTypeFromToken(String token, String secret) {
        return getClaimFromToken(token, "type", secret);
    }

    /**
     * 创建用户Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param secret   密钥
     * @param expiration 过期时间
     * @param roles    角色
     * @return JWT Token
     */
    public static String createUserToken(Long userId, String username, String secret, long expiration, String... roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", "access");
        return generateToken(String.valueOf(userId), claims, secret, expiration);
    }

    /**
     * 创建刷新Token
     *
     * @param userId 用户ID
     * @param secret 密钥
     * @param expiration 过期时间
     * @return 刷新Token
     */
    public static String createRefreshToken(Long userId, String secret, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return generateToken(String.valueOf(userId), claims, secret, expiration);
    }

    /**
     * 验证用户Token
     *
     * @param token JWT Token
     * @param userId 用户ID
     * @param secret 密钥
     * @return 是否有效
     */
    public static boolean validateUserToken(String token, Long userId, String secret) {
        try {
            String subject = getUsernameFromToken(token, secret);
            Long tokenUserId = getClaimFromToken(token, "userId", secret);
            return subject.equals(String.valueOf(userId)) && tokenUserId.equals(userId);
        } catch (Exception e) {
            log.warn("验证用户Token失败: {}", e.getMessage());
            return false;
        }
    }
}