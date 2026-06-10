package com.suresh.paymentsimulator.gateway.request;

import com.suresh.paymentsimulator.gateway.constants.RegexConstants;
import com.suresh.paymentsimulator.gateway.dto.Recipient;
import com.suresh.paymentsimulator.gateway.dto.Sender;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InitiatePaymentRequest {

    @Pattern(regexp = RegexConstants.ALPHA_NUMERIC, message = "Invalid input format")
    @Size(min = 6, max = 36, message = "Invalid input size")
    private String paymentReference;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00",
            message = "Minimum amount is ₹1")
    @Digits(integer = 10, fraction = 2,
            message = "Amount can have up to 2 decimal places")
    private Integer amount;

    @ValidCurrency
    private String currency;

    @Valid
    private Sender sender;

    @Valid
    private Recipient recipient;
}
