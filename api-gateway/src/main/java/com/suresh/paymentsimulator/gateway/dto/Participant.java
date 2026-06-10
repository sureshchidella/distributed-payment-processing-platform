package com.suresh.paymentsimulator.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    private String name;
    private String email;
    private String phoneNumber;
    private Address address;
}
