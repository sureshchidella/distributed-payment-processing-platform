package com.suresh.paymentsimulator.gateway.response;

import com.suresh.paymentsimulator.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for payment initiation.
 * Contains transaction ID, status, currency, and amount.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {

    /** Unique transaction identifier */
    private String id;

    /** Current transaction status (SUCCESS, FAILURE, PENDING, CANCELED) */
    private Status status;

    /** ISO 4217 currency code */
    private String currency;

    /** Transaction amount as string representation */
    private String amount;
}