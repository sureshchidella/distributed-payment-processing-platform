package com.suresh.paymentsimulator.common.repository;

import com.suresh.paymentsimulator.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Transaction entity.
 * Provides CRUD operations and custom queries for transaction persistence.
 * Uses paymentReference (String) as the primary key.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Finds a transaction by its payment reference.
     *
     * @param paymentReference the unique payment reference identifier
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByPaymentReference(String paymentReference);

    /**
     * Checks if a transaction exists with the given payment reference.
     *
     * @param paymentReference the unique payment reference identifier
     * @return true if transaction exists, false otherwise
     */
    boolean existsByPaymentReference(String paymentReference);
}