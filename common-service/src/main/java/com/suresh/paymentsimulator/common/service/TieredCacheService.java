package com.suresh.paymentsimulator.common.service;

import com.suresh.paymentsimulator.common.cache.config.TieredCacheConfig;
import com.suresh.paymentsimulator.common.cache.manager.LocalCacheManager;
import com.suresh.paymentsimulator.common.cache.manager.RedisCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TieredCacheService {

    private final LocalCacheManager localCacheManager;
    private final RedisCacheManager redisCacheManager;

    public <I, T> Optional<T> get(TieredCacheConfig<I, T, ?> cacheConfig, I identifier) {
        Optional<T> localValue = localCacheManager.get(cacheConfig, identifier);

        if (localValue.isPresent()) {
            return localValue;
        }

        Optional<T> redisValue = redisCacheManager.get(cacheConfig, identifier);

        if (redisValue.isPresent()) {
            localCacheManager.put(cacheConfig, identifier, redisValue.get());
            return redisValue;
        }

        Optional<T> databaseValue = cacheConfig.findInDatabase(identifier);
        databaseValue.ifPresent(value -> {
            localCacheManager.put(cacheConfig, identifier, value);
            redisCacheManager.put(cacheConfig, identifier, value);
        });

        return databaseValue;
    }

    public <I, T> void put(TieredCacheConfig<I, T, ?> cacheConfig, I identifier, T value) {
        localCacheManager.put(cacheConfig, identifier, value);
        redisCacheManager.put(cacheConfig, identifier, value);
    }

    public <I, T> void evict(TieredCacheConfig<I, T, ?> cacheConfig, I identifier) {
        localCacheManager.remove(cacheConfig, identifier);
        redisCacheManager.delete(cacheConfig, identifier);
    }
}
