package com.ppcex.risk.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();

        // 为不同的缓存设置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 风控规则缓存 - 5分钟
        cacheConfigurations.put("risk-rules", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // 风控策略缓存 - 10分钟
        cacheConfigurations.put("risk-strategies", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        // 白名单缓存 - 15分钟
        cacheConfigurations.put("risk-whitelist", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));

        // 用户风险评分缓存 - 30分钟
        cacheConfigurations.put("user-risk-score", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));

        // 用户风控状态缓存 - 10分钟
        cacheConfigurations.put("user-risk-status", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        // 风控统计缓存 - 1小时
        cacheConfigurations.put("risk-statistics", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}