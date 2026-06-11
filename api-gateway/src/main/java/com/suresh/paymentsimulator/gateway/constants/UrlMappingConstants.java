package com.suresh.paymentsimulator.gateway.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for URL path mappings in the payment gateway API.
 * Centralizes endpoint paths for maintainability.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlMappingConstants {

    /** Base path for all payment-related endpoints */
    public static final String PAYMENTS = "/payments";

    /** Path for initiating a new payment transaction */
    public static final String INITIATE = "/initiate";
}