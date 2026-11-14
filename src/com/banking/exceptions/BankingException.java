package com.banking.exceptions;

/**
 * Base checked exception for all banking business logic errors.
 * Demonstrates Java Exception Handling, inheritance in exceptions, and clean error messages.
 */
public class BankingException extends Exception {
    private final String errorCode;

    public BankingException(String message) {
        super(message);
        this.errorCode = "BANK_GENERAL_ERROR";
    }

    public BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BANK_INTERNAL_ERROR";
    }

    public BankingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "[" + errorCode + "] " + getMessage();
    }
}
