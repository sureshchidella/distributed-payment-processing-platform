package com.suresh.paymentsimulator.common.util;

import java.security.SecureRandom;

public final class TransactionIdUtil {

    private static final String PREFIX = "txn_";
    private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static final int TOTAL_LENGTH = 40;

    private static final SecureRandom RANDOM = new SecureRandom();

    private TransactionIdUtil() {}

    public static String generateTxnId() {
        int randomPartLength = TOTAL_LENGTH - PREFIX.length();

        StringBuilder sb = new StringBuilder(TOTAL_LENGTH);
        sb.append(PREFIX);

        for (int i = 0; i < randomPartLength; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}