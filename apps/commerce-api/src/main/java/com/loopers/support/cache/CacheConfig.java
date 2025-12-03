package com.loopers.support.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheManager redisCacheManager() {
        // 공통 기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new JdkSerializationRedisSerializer())
                )
                .disableCachingNullValues();

        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // 상품 목록 + 좋아요 정렬 캐시: TTL 5분
        cacheConfigs.put(
                "productLikeSummary",
                defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
