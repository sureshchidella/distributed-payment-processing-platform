package com.suresh.paymentsimulator.common.cache.manager;

import com.suresh.paymentsimulator.common.cache.config.RedisCacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void put(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public <I, T> void put(RedisCacheConfig<I, T> cacheConfig, I identifier, T value) {
        cacheConfig.ttl()
                .ifPresentOrElse(
                        ttl -> put(cacheConfig.cacheKey(identifier), value, ttl),
                        () -> put(cacheConfig.cacheKey(identifier), value)
                );
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(type.cast(value));
    }

    public <I, T> Optional<T> get(RedisCacheConfig<I, T> cacheConfig, I identifier) {
        return get(cacheConfig.cacheKey(identifier), cacheConfig.valueType());
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public <I, T> boolean delete(RedisCacheConfig<I, T> cacheConfig, I identifier) {
        return delete(cacheConfig.cacheKey(identifier));
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public <I, T> boolean exists(RedisCacheConfig<I, T> cacheConfig, I identifier) {
        return exists(cacheConfig.cacheKey(identifier));
    }
}
