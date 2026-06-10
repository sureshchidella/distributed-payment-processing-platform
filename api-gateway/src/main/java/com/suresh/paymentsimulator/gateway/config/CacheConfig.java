package com.suresh.paymentsimulator.gateway.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Tiered cache configuration:
 *   1️⃣ In‑memory cache (Caffeine) – fastest, short‑lived entries.
 *   2️⃣ Redis cache – networked but shared across instances.
 *   3️⃣ PostgreSQL is the authoritative source (accessed via JPA).
 *
 * Spring’s {@link CompositeCacheManager} is used so that @Cacheable looks first in the
 * Caffeine manager, then falls back to Redis.  No JCache, Jakarta Cache or Guava
 * dependencies are required.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // ---------------------------------------------------------------------
    // 1️⃣ In‑memory (Caffeine) cache manager
    // ---------------------------------------------------------------------
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)   // fast‑expiry for first level
                .maximumSize(500));
        return manager;
    }

    // ---------------------------------------------------------------------
    // 2️⃣ Redis cache manager
    // ---------------------------------------------------------------------
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))               // 5‑minute TTL
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration())
                .transactionAware()
                .build();
    }

    // ---------------------------------------------------------------------
    // 3️⃣ Composite manager – Caffeine first, then Redis.
    // ---------------------------------------------------------------------
    @Bean
    public CacheManager compositeCacheManager(CacheManager caffeineCacheManager,
                                             CacheManager redisCacheManager) {
        CompositeCacheManager manager = new CompositeCacheManager(caffeineCacheManager, redisCacheManager);
        // If a key is not found in any delegate, behave like a no‑op cache.
        manager.setFallbackToNoOpCache(true);
        return manager;
    }
}
