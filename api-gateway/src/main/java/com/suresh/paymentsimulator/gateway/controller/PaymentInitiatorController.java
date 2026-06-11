package com.suresh.paymentsimulator.gateway.controller;

import com.suresh.paymentsimulator.gateway.constants.UrlMappingConstants;
import com.suresh.paymentsimulator.gateway.request.InitiatePaymentRequest;
import com.suresh.paymentsimulator.gateway.response.InitiatePaymentResponse;
import com.suresh.paymentsimulator.gateway.service.TransactionCacheService;
import com.suresh.paymentsimulator.common.dto.ApiResponse;
import com.suresh.paymentsimulator.common.entity.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for initiating payment transactions.
 * Handles POST requests to /payments/initiate endpoint.
 * Supports optional Repeat-Flag header for duplicate transaction detection
 * using tiered cache lookup (Caffeine -> Redis -> Database).
 */
@RestController
@RequestMapping(value = UrlMappingConstants.PAYMENTS)
public class PaymentInitiatorController {

    private final TransactionCacheService transactionCacheService;

    /**
     * Constructs a new PaymentInitiatorController.
     *
     * @param transactionCacheService service for tiered cache transaction lookups
     */
    public PaymentInitiatorController(TransactionCacheService transactionCacheService) {
        this.transactionCacheService = transactionCacheService;
    }

    /**
     * Initiates a new payment transaction.
     * If Repeat-Flag header is true, performs a tiered cache lookup
     * to check for existing transaction with the same payment reference.
     *
     * @param initiatePaymentRequest the payment request containing sender, recipient, amount, currency
     * @param repeatFlag             optional header flag to enable duplicate detection (default: false)
     * @return accepted response with empty ApiResponse body
     */
    @PostMapping(value = UrlMappingConstants.INITIATE)
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @RequestBody InitiatePaymentRequest initiatePaymentRequest,
            @RequestHeader(value = "Repeat-Flag", required = false, defaultValue = "false") boolean repeatFlag) {

        if (repeatFlag) {
            String paymentReference = initiatePaymentRequest.getPaymentReference();
            transactionCacheService.getTransaction(paymentReference)
                    .ifPresent(transaction -> {
                    });
        }

        return ResponseEntity.accepted().body(new ApiResponse<>());
    }
}