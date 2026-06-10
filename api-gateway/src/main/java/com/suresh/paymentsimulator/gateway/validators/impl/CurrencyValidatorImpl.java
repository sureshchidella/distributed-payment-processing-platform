package com.suresh.paymentsimulator.gateway.validators.impl;

import com.suresh.paymentsimulator.gateway.validators.ValidCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyValidatorImpl implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> SUPPORTED_CURRENCIES =
            Currency.getAvailableCurrencies()
                    .stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        return SUPPORTED_CURRENCIES.contains(value.toUpperCase());
    }
}
