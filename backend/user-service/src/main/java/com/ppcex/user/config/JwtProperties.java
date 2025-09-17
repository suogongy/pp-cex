package com.ppcex.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "cex.user.jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret = "your-jwt-secret-key-at-least-32-bytes-long-for-security";

    /**
     * 过期时间（毫秒）
     */
    private long expiration = 86400000;

    /**
     * 刷新token过期时间（毫秒）
     */
    private long refreshExpiration = 604800000;

    /**
     * 签发者
     */
    private String issuer = "PPCEX";

    /**
     * 受众
     */
    private String audience = "PPCEX-USER";
}