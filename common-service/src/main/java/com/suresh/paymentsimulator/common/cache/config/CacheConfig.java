package com.suresh.paymentsimulator.common.cache.config;

public interface CacheConfig<I, T> {

    String cacheKey(I identifier);

    Class<T> valueType();
}
