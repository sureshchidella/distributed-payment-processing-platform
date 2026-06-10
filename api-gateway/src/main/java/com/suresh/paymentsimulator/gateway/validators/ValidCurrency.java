package com.suresh.paymentsimulator.gateway.validators;

import com.suresh.paymentsimulator.gateway.validators.impl.CurrencyValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidatorImpl.class)
@Documented
public @interface ValidCurrency {

    String message() default "Invalid currency";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}