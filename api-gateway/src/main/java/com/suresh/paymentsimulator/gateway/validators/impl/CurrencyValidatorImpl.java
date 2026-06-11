package com.suresh.paymentsimulator.gateway.validators.impl;

import com.suresh.paymentsimulator.gateway.service.CurrencyService;
import com.suresh.paymentsimulator.gateway.validators.ValidCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator implementation for {@link ValidCurrency} annotation.
 * Validates that a currency code exists in the loaded currencies.json.
 * Uses CurrencyService which loads currencies at application startup.
 */
public class CurrencyValidatorImpl implements ConstraintValidator<ValidCurrency, String> {

    private CurrencyService currencyService;

    @Autowired
    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Validates if the given value is a valid currency code.
     * Case-insensitive check against currencies loaded from JSON.
     *
     * @param value   the currency code to validate
     * @param context the constraint validator context
     * @return true if valid currency code, false otherwise
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        if (currencyService == null) {
            return false;
        }

        return currencyService.isValidCurrency(value);
    }
}