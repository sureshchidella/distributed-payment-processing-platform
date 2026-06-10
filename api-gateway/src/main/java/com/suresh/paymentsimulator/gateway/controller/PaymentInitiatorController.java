package com.suresh.paymentsimulator.gateway.controller;

import com.suresh.paymentsimulator.gateway.constants.UrlMappingConstants;
import com.suresh.paymentsimulator.gateway.dto.ApiResponse;
import com.suresh.paymentsimulator.gateway.request.InitiatePaymentRequest;
import com.suresh.paymentsimulator.gateway.response.InitiatePaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = UrlMappingConstants.PAYMENTS)
public class PaymentInitiatorController {

    @PostMapping(value = UrlMappingConstants.INITIATE)
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(@RequestBody InitiatePaymentRequest initiatePaymentRequest) {
        return ResponseEntity.accepted().body(new ApiResponse<>());
    }

}
