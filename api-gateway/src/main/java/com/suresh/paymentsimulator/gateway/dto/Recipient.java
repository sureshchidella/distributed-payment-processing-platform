package com.suresh.paymentsimulator.gateway.dto;

import com.suresh.paymentsimulator.gateway.constants.RegexConstants;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipient extends Participant {

    @Pattern(regexp = RegexConstants.NUMERIC_ACCOUNT_NUMBER, message = "Invalid input value")
    private String accountNumber;

    @Pattern(
            regexp = RegexConstants.REGEX_ACCOUNT_NAME,
            message = "Account name can contain only letters and spaces"
    )
    private String accountName;

    @Pattern(
            regexp = RegexConstants.REGEX_IFSC,
            message = "Invalid IFSC code"
    )
    private String ifscCode;
}
