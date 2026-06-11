package com.suresh.paymentsimulator.common.entity;

import com.suresh.paymentsimulator.common.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a payment transaction.
 * Maps to the 'transactions' table in PostgreSQL.
 * Contains full payment details including sender, recipient, amount, currency, and status.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Unique payment reference identifier (primary key).
     * Alphanumeric, 6-36 characters.
     */
    @Id
    @Column(name = "payment_reference", nullable = false, length = 36)
    private String paymentReference;

    /**
     * Transaction amount in minor units (e.g., paise for INR).
     * Precision 19, scale 2 to support up to 2 decimal places.
     */
    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * ISO 4217 currency code (e.g., "INR", "USD", "EUR").
     * 3-character currency code.
     */
    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Current transaction status.
     * One of: SUCCESS, FAILURE, PENDING, CANCELED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    // Sender details
    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "sender_account_number", length = 34)
    private String senderAccountNumber;

    @Column(name = "sender_bank_code", length = 11)
    private String senderBankCode;

    @Column(name = "sender_address_line1", length = 200)
    private String senderAddressLine1;

    @Column(name = "sender_address_line2", length = 200)
    private String senderAddressLine2;

    @Column(name = "sender_city", length = 100)
    private String senderCity;

    @Column(name = "sender_country", length = 2)
    private String senderCountry;

    @Column(name = "sender_postal_code", length = 20)
    private String senderPostalCode;

    // Recipient details
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_account_number", length = 34)
    private String recipientAccountNumber;

    @Column(name = "recipient_bank_code", length = 11)
    private String recipientBankCode;

    @Column(name = "recipient_address_line1", length = 200)
    private String recipientAddressLine1;

    @Column(name = "recipient_address_line2", length = 200)
    private String recipientAddressLine2;

    @Column(name = "recipient_city", length = 100)
    private String recipientCity;

    @Column(name = "recipient_country", length = 2)
    private String recipientCountry;

    @Column(name = "recipient_postal_code", length = 20)
    private String recipientPostalCode;

    /**
     * Timestamp when the transaction was created.
     * Immutable after creation.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the transaction was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}