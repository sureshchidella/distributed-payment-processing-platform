package com.suresh.paymentsimulator.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point for the API Gateway Spring Boot application.
 * Starts the gateway service on port 8080.
 * Handles payment initiation requests with tiered caching.
 */
@SpringBootApplication(scanBasePackages = "com.suresh.paymentsimulator")
@AutoConfigurationPackage(basePackages = "com.suresh.paymentsimulator")
@EnableJpaRepositories(basePackages = "com.suresh.paymentsimulator.common.repository")
public class GatewayApplication {

    /**
     * Application main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
