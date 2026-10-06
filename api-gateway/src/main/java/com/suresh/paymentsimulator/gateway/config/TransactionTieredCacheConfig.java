package com.suresh.paymentsimulator.gateway.config;

import com.suresh.paymentsimulator.common.cache.CacheKeys;
import com.suresh.paymentsimulator.common.cache.config.TieredCacheConfig;
import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.common.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionTieredCacheConfig implements TieredCacheConfig<String, Transaction, TransactionRepository> {

    private final TransactionRepository transactionRepository;

    @Override
    public String cacheKey(String paymentReference) {
        return CacheKeys.TRANSACTION.key(paymentReference);
    }

    @Override
    public Class<Transaction> valueType() {
        return Transaction.class;
    }

    @Override
    public TransactionRepository repository() {
        return transactionRepository;
    }

    @Override
    public Optional<Transaction> findInDatabase(String paymentReference) {
        return transactionRepository.findByPaymentReference(paymentReference);
    }
}
