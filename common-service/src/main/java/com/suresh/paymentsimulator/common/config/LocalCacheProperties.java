package com.suresh.paymentsimulator.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.cache.local")
public class LocalCacheProperties {

    private long maximumSize = 10000;
    private Duration expireAfterWrite = Duration.ofMinutes(30);
}
