package com.suresh.paymentsimulator.gateway.service;

import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.common.service.TieredCacheService;
import com.suresh.paymentsimulator.gateway.config.TransactionTieredCacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionCacheService {

    private final TieredCacheService tieredCacheService;
    private final TransactionTieredCacheConfig transactionCacheConfig;

    public Optional<Transaction> getTransaction(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            return Optional.empty();
        }

        return tieredCacheService.get(transactionCacheConfig, paymentReference);
    }

    public void cacheTransaction(Transaction transaction) {
        if (transaction == null || transaction.getPaymentReference() == null) {
            return;
        }

        tieredCacheService.put(
                transactionCacheConfig,
                transaction.getPaymentReference(),
                transaction
        );
    }

    public void evictTransaction(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            return;
        }

        tieredCacheService.evict(transactionCacheConfig, paymentReference);
    }
}
