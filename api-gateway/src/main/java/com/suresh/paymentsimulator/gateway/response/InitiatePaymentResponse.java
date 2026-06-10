package com.suresh.paymentsimulator.gateway.response;

import com.suresh.paymentsimulator.gateway.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {
    private String id;
    private Status status;
    private String currency;
    private String amount;
}
