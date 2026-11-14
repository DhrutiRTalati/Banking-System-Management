package com.banking.exceptions;

/**
 * Thrown when an account number does not exist in the bank's database.
 */
public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(long accountNumber) {
        super("ACCOUNT_NOT_FOUND", "Account #" + accountNumber + " could not be found.");
    }

    public AccountNotFoundException(String identifier) {
        super("ACCOUNT_NOT_FOUND", "Account associated with '" + identifier + "' could not be found.");
    }
}
