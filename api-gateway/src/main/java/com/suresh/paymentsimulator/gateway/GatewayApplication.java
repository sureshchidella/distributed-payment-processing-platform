package com.suresh.paymentsimulator.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the API Gateway Spring Boot application.
 * Starts the gateway service on port 8080.
 * Handles payment initiation requests with tiered caching.
 */
@SpringBootApplication
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