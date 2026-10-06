package com.suresh.paymentsimulator.common.cache.config;

import java.time.Duration;
import java.util.Optional;

public interface RedisCacheConfig<I, T> extends CacheConfig<I, T> {

    default Optional<Duration> ttl() {
        return Optional.empty();
    }
}
