package com.suresh.paymentsimulator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical address for payment participants.
 * Used by both sender and recipient entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /** Street address line (house number, street name) */
    private String street;

    /** City name */
    private String city;

    /** State or province */
    private String state;

    /** Postal/ZIP code */
    private String zip;

    /** ISO 3166-1 alpha-2 country code (e.g., "IN", "US", "GB") */
    private String country;
}