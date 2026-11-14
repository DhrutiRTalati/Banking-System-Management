package com.banking.exceptions;

/**
 * Thrown when trying to register an email address that is already registered.
 */
public class DuplicateUserException extends BankingException {
    public DuplicateUserException(String email) {
        super("DUPLICATE_USER", "A customer account with email '" + email + "' is already registered.");
    }
}
