package com.suresh.paymentsimulator.common.cache;

public enum CacheKeys {

    PAYMENT("payment:"),
    IDEMPOTENCY("idempotency:"),
    TRANSACTION("transaction:"),
    USER("user:");

    private final String prefix;

    CacheKeys(String prefix) {
        this.prefix = prefix;
    }

    public String key(String identifier) {
        return prefix + identifier;
    }
}
