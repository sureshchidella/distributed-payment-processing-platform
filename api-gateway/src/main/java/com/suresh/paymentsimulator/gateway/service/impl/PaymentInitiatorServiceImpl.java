package com.suresh.paymentsimulator.gateway.service.impl;

import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.common.repository.TransactionRepository;
import com.suresh.paymentsimulator.gateway.mapper.InitiateTransactionResponseMapper;
import com.suresh.paymentsimulator.gateway.mapper.TransactionMapper;
import com.suresh.paymentsimulator.gateway.request.InitiatePaymentRequest;
import com.suresh.paymentsimulator.gateway.response.InitiatePaymentResponse;
import com.suresh.paymentsimulator.gateway.service.PaymentInitiatorService;
import com.suresh.paymentsimulator.gateway.service.TransactionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.suresh.paymentsimulator.common.util.TransactionIdUtil.generateTxnId;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiatorServiceImpl implements PaymentInitiatorService {

    private final TransactionCacheService transactionCacheService;
    private final InitiateTransactionResponseMapper responseMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest initiatePaymentRequest, boolean repeatFlag) {
        if (repeatFlag) {
            Optional<Transaction> transaction = transactionCacheService.getTransaction(initiatePaymentRequest.getPaymentReference());
            if (transaction.isPresent()) {
                log.info("found transaction with reference {}", initiatePaymentRequest.getPaymentReference());
                return responseMapper.map(transaction.get());
            }
        } else {
            log.info("processing transaction with reference {}", initiatePaymentRequest.getPaymentReference());
            Transaction transaction = transactionMapper.mapToEntity(initiatePaymentRequest);
            transaction.setId(generateTxnId());

            transactionRepository.save(transaction);
            log.info("saved transaction with id {}", transaction.getId());

            return responseMapper.map(transaction);
        }
        return null;
    }
}
