package com.suresh.paymentsimulator.gateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suresh.paymentsimulator.common.entity.Currency;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for loading and managing currency data from currencies.json.
 * Loads all supported currencies at application startup and provides
 * validation and lookup functionality.
 */
@Service
public class CurrencyService {

    private static final String CURRENCIES_JSON_PATH = "data/currencies.json";

    private final Map<String, Currency> currencies = new HashMap<>();
    private Set<String> currencyCodes;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads currencies from the JSON file at application startup.
     *
     * @throws RuntimeException if the JSON file cannot be loaded or parsed
     */
    @PostConstruct
    public void loadCurrencies() {
        try {
            ClassPathResource resource = new ClassPathResource(CURRENCIES_JSON_PATH);
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Currency> loadedCurrencies = objectMapper.readValue(
                        inputStream,
                        new TypeReference<Map<String, Currency>>() {}
                );
                currencies.putAll(loadedCurrencies);
                currencyCodes = currencies.keySet().stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toUnmodifiableSet());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currencies from " + CURRENCIES_JSON_PATH, e);
        }
    }

    /**
     * Checks if a currency code is valid (exists in the loaded currencies).
     *
     * @param currencyCode the currency code to validate (case-insensitive)
     * @return true if valid currency code, false otherwise
     */
    public boolean isValidCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return false;
        }
        return currencyCodes.contains(currencyCode.toUpperCase());
    }

    /**
     * Gets the currency details for a given currency code.
     *
     * @param currencyCode the currency code (case-insensitive)
     * @return the Currency object if found, null otherwise
     */
    public Currency getCurrency(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }
        return currencies.get(currencyCode.toUpperCase());
    }

    /**
     * Gets all loaded currency codes.
     *
     * @return unmodifiable set of all currency codes
     */
    public Set<String> getAllCurrencyCodes() {
        return currencyCodes;
    }

    /**
     * Gets all loaded currencies.
     *
     * @return unmodifiable map of currency code to Currency object
     */
    public Map<String, Currency> getAllCurrencies() {
        return Map.copyOf(currencies);
    }

    /**
     * Gets the number of loaded currencies.
     *
     * @return count of loaded currencies
     */
    public int getCurrencyCount() {
        return currencies.size();
    }
}