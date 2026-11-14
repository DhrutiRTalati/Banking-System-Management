package com.banking.exceptions;

/**
 * Thrown when credentials fail authentication (invalid email/password, or missing user).
 */
public class AuthenticationException extends BankingException {
    public AuthenticationException(String message) {
        super("AUTH_FAILED", message);
    }
}
