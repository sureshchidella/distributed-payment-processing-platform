package com.suresh.paymentsimulator.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocalCacheProperties.class)
public class CacheInfrastructureConfig {

    @Bean
    public Cache<String, Object> localCache(LocalCacheProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(properties.getExpireAfterWrite())
                .build();
    }
}
