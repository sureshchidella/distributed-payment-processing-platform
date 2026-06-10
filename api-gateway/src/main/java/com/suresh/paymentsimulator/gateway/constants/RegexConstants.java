package com.suresh.paymentsimulator.gateway.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexConstants {

    public static final String ALPHA_NUMERIC = "^[A-Za-z0-9]+$";
    public static final String NUMERIC_ACCOUNT_NUMBER = "^\\d{6,20}$";
    public static final String REGEX_ACCOUNT_NAME = "^[A-Za-z ]+$";
    public static final String REGEX_IFSC = "^[A-Z]{4}0[A-Z0-9]{6}$";
}
