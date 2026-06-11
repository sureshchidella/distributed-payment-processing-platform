package com.suresh.paymentsimulator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic API response wrapper for consistent response structure across endpoints.
 *
 * @param <T> the type of the response data payload
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {

    /** Response status indicator (e.g., "SUCCESS", "ERROR", "ACCEPTED") */
    private String status;

    /** Human-readable message describing the response */
    private String message;

    /** Response data payload of generic type T */
    private T data;
}