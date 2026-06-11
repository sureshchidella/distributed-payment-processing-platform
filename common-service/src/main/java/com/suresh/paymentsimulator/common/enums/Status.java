package com.suresh.paymentsimulator.common.enums;

/**
 * Enumeration of possible transaction statuses.
 * Each status has a human-readable display value.
 */
public enum Status {

    /** Transaction completed successfully */
    SUCCESS("Success"),

    /** Transaction failed */
    FAILURE("Failure"),

    /** Transaction is pending processing */
    PENDING("Pending"),

    /** Transaction was cancelled */
    CANCELED("Cancelled");

    /** Human-readable display value for the status */
    final String status;

    /**
     * Constructs a Status with the given display value.
     *
     * @param status the human-readable display string
     */
    Status(String status) {
        this.status = status;
    }
}