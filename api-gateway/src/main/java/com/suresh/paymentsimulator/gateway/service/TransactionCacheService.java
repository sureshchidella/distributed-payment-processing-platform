package com.suresh.paymentsimulator.gateway.service;

import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.common.repository.TransactionRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing transaction data across a three-tier cache hierarchy.
 * Implements the cache-aside pattern with tiered lookup:
 * <ol>
 *   <li>L1: In-memory Caffeine cache (fastest, ~10s TTL)</li>
 *   <li>L2: Redis distributed cache (~5min TTL)</li>
 *   <li>L3: PostgreSQL database (source of truth)</li>
 * </ol>
 * On cache miss, data is promoted from lower tiers to higher tiers.
 */
@Service
public class TransactionCacheService {

    private static final String TRANSACTION_CACHE_NAME = "transactionCache";

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionRepository transactionRepository;

    /**
     * Constructs a new TransactionCacheService with required dependencies.
     *
     * @param cacheManager         the composite cache manager (Caffeine + Redis)
     * @param redisTemplate        Redis template for direct Redis operations
     * @param transactionRepository JPA repository for database access
     */
    public TransactionCacheService(CacheManager cacheManager,
                                    RedisTemplate<String, Object> redisTemplate,
                                    TransactionRepository transactionRepository) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves a transaction by payment reference using tiered cache lookup.
     * Checks caches in order: Caffeine (L1) -> Redis (L2) -> Database (L3).
     * On cache hit at L2 or L3, promotes the data to higher tiers.
     *
     * @param paymentReference the unique payment reference identifier
     * @return Optional containing the transaction if found, empty otherwise
     */
    public Optional<Transaction> getTransaction(String paymentReference) {
        Cache cache = cacheManager.getCache(TRANSACTION_CACHE_NAME);
        if (cache != null) {
            Transaction cached = cache.get(paymentReference, Transaction.class);
            if (cached != null) {
                return Optional.of(cached);
            }
        }

        String redisKey = "transaction:" + paymentReference;
        Object redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            Transaction transaction = (Transaction) redisValue;
            if (cache != null) {
                cache.put(paymentReference, transaction);
            }
            return Optional.of(transaction);
        }

        Optional<Transaction> fromDb = transactionRepository.findByPaymentReference(paymentReference);
        if (fromDb.isPresent()) {
            Transaction transaction = fromDb.get();
            if (cache != null) {
                cache.put(paymentReference, transaction);
            }
            redisTemplate.opsForValue().set(redisKey, transaction);
            return Optional.of(transaction);
        }

        return Optional.empty();
    }

    /**
     * Stores a transaction in both L1 (Caffeine) and L2 (Redis) caches.
     *
     * @param paymentReference the unique payment reference identifier
     * @param transaction      the transaction to cache
     */
    public void putTransaction(String paymentReference, Transaction transaction) {
        Cache cache = cacheManager.getCache(TRANSACTION_CACHE_NAME);
        if (cache != null) {
            cache.put(paymentReference, transaction);
        }
        String redisKey = "transaction:" + paymentReference;
        redisTemplate.opsForValue().set(redisKey, transaction);
    }

    /**
     * Evicts a transaction from both L1 (Caffeine) and L2 (Redis) caches.
     *
     * @param paymentReference the unique payment reference identifier
     */
    public void evictTransaction(String paymentReference) {
        Cache cache = cacheManager.getCache(TRANSACTION_CACHE_NAME);
        if (cache != null) {
            cache.evict(paymentReference);
        }
        String redisKey = "transaction:" + paymentReference;
        redisTemplate.delete(redisKey);
    }
}