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
     * JWT密钥
     */
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "your-jwt-secret-key-at-least-32-bytes-long-for-security");

    /**
     * 过期时间（毫秒）
     */
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时

    /**
     * 生成密钥
     */
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * 生成JWT Token
     *
     * @param subject 主题
     * @param claims  声明
     * @return JWT Token
     */
    public static String generateToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成JWT Token
     *
     * @param subject 主题
     * @return JWT Token
     */
    public static String generateToken(String subject) {
        return generateToken(subject, new HashMap<>());
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证JWT Token
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
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
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从Token中获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从Token中获取指定声明
     *
     * @param token          JWT Token
     * @param claimsResolver 声明解析器
     * @param <T>            声明类型
     * @return 声明值
     */
    public static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中获取指定声明
     *
     * @param token     JWT Token
     * @param claimName 声明名称
     * @param <T>       声明类型
     * @return 声明值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getClaimFromToken(String token, String claimName) {
        final Claims claims = parseToken(token);
        return (T) claims.get(claimName);
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 刷新Token
     *
     * @param token JWT Token
     * @return 新的JWT Token
     */
    public static String refreshToken(String token) {
        final Claims claims = parseToken(token);
        return generateToken(claims.getSubject(), claims);
    }

    /**
     * 获取Token剩余有效期（毫秒）
     *
     * @param token JWT Token
     * @return 剩余有效期
     */
    public static long getTokenRemainingTime(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 检查Token是否即将过期（剩余时间小于5分钟）
     *
     * @param token JWT Token
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon(String token) {
        long remainingTime = getTokenRemainingTime(token);
        return remainingTime > 0 && remainingTime < 5 * 60 * 1000;
    }

    /**
     * 获取Token签发时间
     *
     * @param token JWT Token
     * @return 签发时间
     */
    public static Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 获取Token ID
     *
     * @param token JWT Token
     * @return Token ID
     */
    public static String getIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    /**
     * 获取Token签发者
     *
     * @param token JWT Token
     * @return 签发者
     */
    public static String getIssuerFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuer);
    }

    /**
     * 获取Token受众
     *
     * @param token JWT Token
     * @return 受众集合
     */
    public static java.util.Set<String> getAudienceFromToken(String token) {
        return getClaimFromToken(token, Claims::getAudience);
    }

    /**
     * 获取Token类型
     *
     * @param token JWT Token
     * @return Token类型
     */
    public static String getTypeFromToken(String token) {
        return getClaimFromToken(token, "type");
    }

    /**
     * 创建用户Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色
     * @return JWT Token
     */
    public static String createUserToken(Long userId, String username, String... roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", "access");
        return generateToken(String.valueOf(userId), claims);
    }

    /**
     * 创建刷新Token
     *
     * @param userId 用户ID
     * @return 刷新Token
     */
    public static String createRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return generateToken(String.valueOf(userId), claims);
    }

    /**
     * 验证用户Token
     *
     * @param token JWT Token
     * @param userId 用户ID
     * @return 是否有效
     */
    public static boolean validateUserToken(String token, Long userId) {
        try {
            String subject = getUsernameFromToken(token);
            Long tokenUserId = getClaimFromToken(token, "userId");
            return subject.equals(String.valueOf(userId)) && tokenUserId.equals(userId);
        } catch (Exception e) {
            log.warn("验证用户Token失败: {}", e.getMessage());
            return false;
        }
    }
}