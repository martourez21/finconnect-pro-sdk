package com.codedstreams.finconnectpro.sdk.util;

import com.codedstreams.finconnectpro.sdk.dto.ConnectionConfigDto;
import com.codedstreams.finconnectpro.sdk.dto.TransactionRequestDto;

import java.math.BigDecimal;

/**
 * Utility class for validation operations.
 * <p>
 * This class provides common validation methods used throughout the SDK
 * to ensure data integrity and business rule compliance.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public final class ValidationUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates a connection configuration object.
     *
     * @param config the connection configuration to validate
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public static void validateConnectionConfig(ConnectionConfigDto config) {
        if (config == null) {
            throw new IllegalArgumentException("Connection configuration cannot be null");
        }
        if (config.getProtocolType() == null) {
            throw new IllegalArgumentException("Protocol type cannot be null");
        }
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (config.getPort() == null || config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (config.getTimeoutMs() != null && config.getTimeoutMs() < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
    }

    /**
     * Validates a transaction request object.
     *
     * @param request the transaction request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    public static void validateTransactionRequest(TransactionRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        if (request.getCurrency().length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-character ISO code");
        }
    }

    /**
     * Validates an IBAN (International Bank Account Number).
     *
     * @param iban the IBAN to validate
     * @return true if the IBAN is valid, false otherwise
     */
    public static boolean isValidIban(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }

        // Basic format validation - in practice, use a proper IBAN validation library
        return iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}");
    }

    /**
     * Validates a BIC (Business Identifier Code).
     *
     * @param bic the BIC to validate
     * @return true if the BIC is valid, false otherwise
     */
    public static boolean isValidBic(String bic) {
        if (bic == null || bic.length() != 8 && bic.length() != 11) {
            return false;
        }

        return bic.matches("[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?");
    }

    /**
     * Validates a currency code (ISO 4217).
     *
     * @param currency the currency code to validate
     * @return true if the currency code is valid, false otherwise
     */
    public static boolean isValidCurrencyCode(String currency) {
        if (currency == null || currency.length() != 3) {
            return false;
        }

        return currency.matches("[A-Z]{3}");
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Validates that an object is not null.
     *
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the object is null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a number is positive.
     *
     * @param number the number to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the number is not positive
     */
    public static void validatePositive(Number number, String fieldName) {
        if (number == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        if (number.doubleValue() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}
