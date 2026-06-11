package com.suresh.paymentsimulator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class representing a payment participant (sender or recipient).
 * Contains common contact and address information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    /** Full name of the participant */
    private String name;

    /** Email address for notifications */
    private String email;

    /** Phone number in international format */
    private String phoneNumber;

    /** Physical address of the participant */
    private Address address;
}