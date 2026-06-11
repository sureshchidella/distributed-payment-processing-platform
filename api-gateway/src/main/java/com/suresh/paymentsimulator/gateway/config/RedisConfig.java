package com.suresh.paymentsimulator.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis configuration using Jedis client.
 * Provides connection pooling and RedisTemplate for cache operations.
 * Used as L2 cache in the tiered caching strategy.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    /**
     * Creates a Jedis connection pool with configured settings.
     * Pool validates connections on borrow and return for reliability.
     *
     * @return configured JedisPool for Redis connections
     */
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        return new JedisPool(poolConfig, redisHost, redisPort, redisTimeout);
    }

    /**
     * Creates a RedisTemplate for generic key-value operations.
     * Uses default serialization (JDK serialization for keys/values).
     *
     * @param jedisConnectionFactory the Jedis connection factory
     * @return RedisTemplate for cache operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }
}