package com.suresh.paymentsimulator.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisConnectionProperties.class)
public class RedisConnectionConfig {

    private final RedisConnectionProperties redisProperties;

    @Value("${spring.data.redis.host:${spring.redis.host:localhost}}")
    private String host;

    @Value("${spring.data.redis.port:${spring.redis.port:6379}}")
    private int port;

    @Value("${spring.data.redis.password:${spring.redis.password:}}")
    private String password;

    @Value("${spring.data.redis.timeout:${spring.redis.timeout:10s}}")
    private Duration commandTimeout;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(host);
        standalone.setPort(port);

        if (password != null && !password.isBlank()) {
            standalone.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration clientConfiguration =
                LettuceClientConfiguration.builder()
                        .commandTimeout(commandTimeout)
                        .shutdownTimeout(redisProperties.getShutdownTimeout())
                        .build();

        return new LettuceConnectionFactory(standalone, clientConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
