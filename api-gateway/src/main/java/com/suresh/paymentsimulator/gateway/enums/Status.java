package com.suresh.paymentsimulator.gateway.enums;

public enum Status {
    SUCCESS("Success"),
    FAILURE("Failure"),
    PENDING("Pending"),
    CANCELED("Cancelled");

    final String status;

    Status(String success) {
        this.status = success;
    }
}
