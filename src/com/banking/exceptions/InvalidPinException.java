package com.banking.exceptions;

/**
 * Thrown when the 4-digit security PIN provided fails verification.
 */
public class InvalidPinException extends BankingException {
    public InvalidPinException() {
        super("INVALID_PIN", "The security PIN entered is incorrect. Access denied.");
    }

    public InvalidPinException(String message) {
        super("INVALID_PIN", message);
    }
}
