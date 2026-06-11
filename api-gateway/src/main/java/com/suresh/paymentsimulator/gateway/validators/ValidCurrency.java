package com.suresh.paymentsimulator.gateway.validators;

import com.suresh.paymentsimulator.gateway.validators.impl.CurrencyValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation annotation for validating ISO 4217 currency codes.
 * Delegates validation to {@link CurrencyValidatorImpl}.
 * Checks if the currency code exists in the set of available currencies.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidatorImpl.class)
@Documented
public @interface ValidCurrency {

    /** Error message when validation fails */
    String message() default "Invalid currency";

    /** Validation groups */
    Class<?>[] groups() default {};

    /** Payload for constraint metadata */
    Class<? extends Payload>[] payload() default {};
}