package com.suresh.paymentsimulator.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.redis")
public class RedisConnectionProperties {

    private Duration shutdownTimeout = Duration.ofSeconds(15);
}
