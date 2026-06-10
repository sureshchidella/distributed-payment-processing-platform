package com.suresh.paymentsimulator.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
}
