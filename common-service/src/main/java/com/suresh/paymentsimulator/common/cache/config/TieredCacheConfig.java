package com.suresh.paymentsimulator.common.cache.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TieredCacheConfig<I, T, R extends JpaRepository<T, ?>> extends LocalCacheConfig<I, T>, RedisCacheConfig<I, T> {

    R repository();

    Optional<T> findInDatabase(I identifier);
}
