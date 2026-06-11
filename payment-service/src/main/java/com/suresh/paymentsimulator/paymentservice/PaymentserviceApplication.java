package com.suresh.paymentsimulator.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Payment Service Spring Boot application.
 * Starts the payment processing service on port 8081.
 * Provides data access layer for transaction persistence.
 */
@SpringBootApplication
public class PaymentserviceApplication {

    /**
     * Application main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentserviceApplication.class, args);
    }
}