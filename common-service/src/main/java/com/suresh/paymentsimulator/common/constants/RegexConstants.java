package com.suresh.paymentsimulator.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for regular expression patterns used in validation.
 * Provides reusable regex patterns for payment-related fields.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexConstants {

    /**
     * Alphanumeric pattern: only letters and digits allowed.
     * Used for payment reference validation.
     */
    public static final String ALPHA_NUMERIC = "^[A-Za-z0-9]+$";

    /**
     * Numeric account number pattern: 6 to 20 digits.
     * Used for sender/recipient bank account number validation.
     */
    public static final String NUMERIC_ACCOUNT_NUMBER = "^\\d{6,20}$";

    /**
     * Account name pattern: letters and spaces only.
     * Used for sender/recipient account holder name validation.
     */
    public static final String REGEX_ACCOUNT_NAME = "^[A-Za-z ]+$";

    /**
     * IFSC code pattern: 4 uppercase letters + '0' + 6 alphanumeric characters.
     * Indian Financial System Code format for bank branch identification.
     * Example: SBIN0001234
     */
    public static final String REGEX_IFSC = "^[A-Z]{4}0[A-Z0-9]{6}$";
}