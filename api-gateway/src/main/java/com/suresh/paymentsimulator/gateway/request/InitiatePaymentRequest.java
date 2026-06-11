package com.suresh.paymentsimulator.gateway.request;

import com.suresh.paymentsimulator.common.constants.RegexConstants;
import com.suresh.paymentsimulator.common.dto.Recipient;
import com.suresh.paymentsimulator.common.dto.Sender;
import com.suresh.paymentsimulator.gateway.validators.ValidCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for initiating a new payment transaction.
 * Contains payment reference, amount, currency, sender and recipient details.
 * Validated using Bean Validation annotations.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InitiatePaymentRequest {

    /**
     * Unique payment reference identifier.
     * Alphanumeric, 6-36 characters (validated by ALPHA_NUMERIC and Size).
     */
    @Pattern(regexp = RegexConstants.ALPHA_NUMERIC, message = "Invalid input format")
    @Size(min = 6, max = 36, message = "Invalid input size")
    private String paymentReference;

    /**
     * Transaction amount in minor units (e.g., paise for INR).
     * Required, minimum 1.00, up to 2 decimal places.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum amount is ₹1")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 2 decimal places")
    private Integer amount;

    /**
     * ISO 4217 currency code (e.g., "INR", "USD", "EUR").
     * Validated by @ValidCurrency custom validator.
     */
    @ValidCurrency
    private String currency;

    /**
     * Sender (payer) details including bank account and contact info.
     * Validated recursively via @Valid.
     */
    @Valid
    private Sender sender;

    /**
     * Recipient (payee) details including bank account and contact info.
     * Validated recursively via @Valid.
     */
    @Valid
    private Recipient recipient;
}