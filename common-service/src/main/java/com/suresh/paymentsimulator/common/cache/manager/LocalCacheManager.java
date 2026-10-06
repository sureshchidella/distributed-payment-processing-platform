package com.suresh.paymentsimulator.common.cache.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.suresh.paymentsimulator.common.cache.config.LocalCacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocalCacheManager implements LocalCache<Object> {

    private final Cache<String, Object> cache;

    @Override
    public Object get(String key) {
        return cache.getIfPresent(key);
    }

    public <I, T> Optional<T> get(LocalCacheConfig<I, T> cacheConfig, I identifier) {
        Object value = get(cacheConfig.cacheKey(identifier));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(cacheConfig.valueType().cast(value));
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public <I, T> void put(LocalCacheConfig<I, T> cacheConfig, I identifier, T value) {
        put(cacheConfig.cacheKey(identifier), value);
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
    }

    public <I, T> void remove(LocalCacheConfig<I, T> cacheConfig, I identifier) {
        remove(cacheConfig.cacheKey(identifier));
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public Object delete(String key) {
        Object value = cache.getIfPresent(key);

        if (value != null) {
            cache.invalidate(key);
        }

        return value;
    }

    public <I, T> Optional<T> delete(LocalCacheConfig<I, T> cacheConfig, I identifier) {
        Object value = delete(cacheConfig.cacheKey(identifier));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(cacheConfig.valueType().cast(value));
    }
}
