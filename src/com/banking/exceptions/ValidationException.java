package com.banking.exceptions;

/**
 * Thrown when user inputs fail format or business validation checks.
 */
public class ValidationException extends BankingException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
