package com.ppcex.match.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        GenericFastJsonRedisSerializer fastJsonSerializer = new GenericFastJsonRedisSerializer();
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    public static class GenericFastJsonRedisSerializer implements RedisSerializer<Object> {
        private final Charset charset;

        public GenericFastJsonRedisSerializer() {
            this(Charset.forName("UTF-8"));
        }

        public GenericFastJsonRedisSerializer(Charset charset) {
            this.charset = charset;
        }

        @Override
        public byte[] serialize(Object object) {
            if (object == null) {
                return new byte[0];
            }
            return JSON.toJSONString(object, JSONWriter.Feature.WriteClassName).getBytes(charset);
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            String str = new String(bytes, charset);
            return JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        }
    }
}