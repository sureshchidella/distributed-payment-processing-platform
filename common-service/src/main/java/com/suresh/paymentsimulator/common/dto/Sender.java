package com.suresh.paymentsimulator.common.dto;

import com.suresh.paymentsimulator.common.constants.RegexConstants;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the sender (payer) in a payment transaction.
 * Extends Participant with bank account details specific to Indian banking.
 * Validates account number, account name, and IFSC code format.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sender extends Participant {

    /**
     * Bank account number of the sender.
     * Must be 6-20 digits (validated by NUMERIC_ACCOUNT_NUMBER pattern).
     */
    @Pattern(regexp = RegexConstants.NUMERIC_ACCOUNT_NUMBER, message = "Invalid input value")
    private String accountNumber;

    /**
     * Account holder name as registered with the bank.
     * Letters and spaces only (validated by REGEX_ACCOUNT_NAME pattern).
     */
    @Pattern(
            regexp = RegexConstants.REGEX_ACCOUNT_NAME,
            message = "Account name can contain only letters and spaces"
    )
    private String accountName;

    /**
     * IFSC (Indian Financial System Code) of the sender's bank branch.
     * Format: 4 letters + '0' + 6 alphanumeric (validated by REGEX_IFSC pattern).
     * Example: SBIN0001234
     */
    @Pattern(
            regexp = RegexConstants.REGEX_IFSC,
            message = "Invalid IFSC code"
    )
    private String ifscCode;
}