package com.suresh.paymentsimulator.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for tiered caching strategy.
 * Provides a three-layer cache hierarchy:
 * <ol>
 *   <li>L1: In-memory Caffeine cache (10s TTL, max 500 entries)</li>
 *   <li>L2: Distributed Redis cache (5min TTL)</li>
 *   <li>L3: Database (PostgreSQL)</li>
 * </ol>
 * Uses Spring's CompositeCacheManager to chain Caffeine and Redis cache managers.
 */
@Configuration
@EnableCaching
public class TieredCacheConfig {

    /**
     * Creates the in-memory Caffeine cache manager (L1 cache).
     * Fast, local cache with short TTL for frequently accessed data.
     *
     * @return configured Caffeine cache manager with transactionCache
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("transactionCache");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(500));
        return manager;
    }

    /**
     * Creates the Redis cache configuration with JSON serialization (L2 cache).
     * Uses RedisSerializer.json() for type-safe JSON serialization (Spring Data Redis 4.0+).
     *
     * @return Redis cache configuration with 5-minute TTL and null value disabling
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        RedisSerializer<Object> serializer = RedisSerializer.json();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    /**
     * Creates the Redis cache manager using the configured connection factory.
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return configured Redis cache manager with transaction awareness
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration())
                .transactionAware()
                .build();
    }

    /**
     * Creates a composite cache manager that checks Caffeine first, then falls back to Redis.
     * Provides a tiered caching strategy: L1 (in-memory) -> L2 (distributed Redis).
     *
     * @param caffeineCacheManager the in-memory cache manager
     * @param redisCacheManager    the distributed Redis cache manager
     * @return composite cache manager with no-op fallback
     */
    @Bean
    public CacheManager compositeCacheManager(CacheManager caffeineCacheManager,
                                              CacheManager redisCacheManager) {
        CompositeCacheManager manager = new CompositeCacheManager(caffeineCacheManager, redisCacheManager);
        manager.setFallbackToNoOpCache(true);
        return manager;
    }
}