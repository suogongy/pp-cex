package com.ppcex.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

    private Security security;
    private RateLimit rateLimit;
    private Cache cache;

    public static class Security {
        private String[] permitAll = {
            "/doc.html",
            "/doc.html/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/v3/api-docs/swagger-config",
            "/webjars/**",
            "/actuator/**",
            "/user/v3/api-docs/**",
            "/trade/v3/api-docs/**",
            "/wallet/v3/api-docs/**",
            "/finance/v3/api-docs/**",
            "/market/v3/api-docs/**",
            "/risk/v3/api-docs/**",
            "/notify/v3/api-docs/**",
            "/match/v3/api-docs/**",
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/fallback/**"
        };
        private String[] ipWhitelist = {"127.0.0.1", "0:0:0:0:0:0:0:1", "192.168.0.0/16", "10.0.0.0/8"};

        public String[] getPermitAll() {
            return permitAll;
        }

        public void setPermitAll(String[] permitAll) {
            this.permitAll = permitAll;
        }

        public String[] getIpWhitelist() {
            return ipWhitelist;
        }

        public void setIpWhitelist(String[] ipWhitelist) {
            this.ipWhitelist = ipWhitelist;
        }
    }

    public static class RateLimit {
        private int defaultReplenishRate = 100;
        private int defaultBurstCapacity = 200;
        private int userReplenishRate = 50;
        private int userBurstCapacity = 100;
        private int ipReplenishRate = 200;
        private int ipBurstCapacity = 400;

        public int getDefaultReplenishRate() {
            return defaultReplenishRate;
        }

        public void setDefaultReplenishRate(int defaultReplenishRate) {
            this.defaultReplenishRate = defaultReplenishRate;
        }

        public int getDefaultBurstCapacity() {
            return defaultBurstCapacity;
        }

        public void setDefaultBurstCapacity(int defaultBurstCapacity) {
            this.defaultBurstCapacity = defaultBurstCapacity;
        }

        public int getUserReplenishRate() {
            return userReplenishRate;
        }

        public void setUserReplenishRate(int userReplenishRate) {
            this.userReplenishRate = userReplenishRate;
        }

        public int getUserBurstCapacity() {
            return userBurstCapacity;
        }

        public void setUserBurstCapacity(int userBurstCapacity) {
            this.userBurstCapacity = userBurstCapacity;
        }

        public int getIpReplenishRate() {
            return ipReplenishRate;
        }

        public void setIpReplenishRate(int ipReplenishRate) {
            this.ipReplenishRate = ipReplenishRate;
        }

        public int getIpBurstCapacity() {
            return ipBurstCapacity;
        }

        public void setIpBurstCapacity(int ipBurstCapacity) {
            this.ipBurstCapacity = ipBurstCapacity;
        }
    }

    public static class Cache {
        private int authTtl = 3600; // 1小时
        private int routeTtl = 1800; // 30分钟
        private int rateLimitTtl = 300; // 5分钟

        public int getAuthTtl() {
            return authTtl;
        }

        public void setAuthTtl(int authTtl) {
            this.authTtl = authTtl;
        }

        public int getRouteTtl() {
            return routeTtl;
        }

        public void setRouteTtl(int routeTtl) {
            this.routeTtl = routeTtl;
        }

        public int getRateLimitTtl() {
            return rateLimitTtl;
        }

        public void setRateLimitTtl(int rateLimitTtl) {
            this.rateLimitTtl = rateLimitTtl;
        }
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
}