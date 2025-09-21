package com.ppcex.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cex.jwt")
public class JwtProperties {

    private boolean enabled = true;
    private String secret;
    private Long expiration;
    private Long refreshExpiration;
    private String issuer = "PPCEX";
}