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
            "/api/v1/gateway/health",
            // OpenAPI文档路径 - Knife4j发现的路径
            "/api/v1/user/v3/api-docs/**",
            "/api/v1/trade/v3/api-docs/**",
            "/api/v1/wallet/v3/api-docs/**",
            "/api/v1/finance/v3/api-docs/**",
            "/api/v1/market/v3/api-docs/**",
            "/api/v1/risk/v3/api-docs/**",
            "/api/v1/notify/v3/api-docs/**",
            "/api/v1/match/v3/api-docs/**",
            // 直接服务名称的OpenAPI路径
            "/user-service/v3/api-docs/**",
            "/trade-service/v3/api-docs/**",
            "/wallet-service/v3/api-docs/**",
            "/finance-service/v3/api-docs/**",
            "/market-service/v3/api-docs/**",
            "/risk-service/v3/api-docs/**",
            "/notify-service/v3/api-docs/**",
            "/match-service/v3/api-docs/**"
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
            this.authTtl = validateTtl("authTtl", authTtl, 60, 86400); // 1分钟到24小时
        }

        public int getRouteTtl() {
            return routeTtl;
        }

        public void setRouteTtl(int routeTtl) {
            this.routeTtl = validateTtl("routeTtl", routeTtl, 60, 3600); // 1分钟到1小时
        }

        public int getRateLimitTtl() {
            return rateLimitTtl;
        }

        public void setRateLimitTtl(int rateLimitTtl) {
            this.rateLimitTtl = validateTtl("rateLimitTtl", rateLimitTtl, 60, 3600); // 1分钟到1小时
        }

        /**
         * 验证TTL值是否在合理范围内
         */
        private int validateTtl(String name, int value, int min, int max) {
            if (value < min) {
                throw new IllegalArgumentException(String.format("%s值%d秒小于最小值%d秒", name, value, min));
            }
            if (value > max) {
                throw new IllegalArgumentException(String.format("%s值%d秒大于最大值%d秒", name, value, max));
            }
            return value;
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