package com.suresh.paymentsimulator.gateway.service;

import com.suresh.paymentsimulator.gateway.request.InitiatePaymentRequest;
import com.suresh.paymentsimulator.gateway.response.InitiatePaymentResponse;
import org.springframework.stereotype.Service;

@Service
public interface PaymentInitiatorService {
    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest initiatePaymentRequest, boolean repeatFlag);
}
