package com.suresh.paymentsimulator.common.cache.manager;

public interface LocalCache<T> {
    T get(String key);

    void put(String key, T value);

    void remove(String key);

    void clear();

    T delete(String key);
}
