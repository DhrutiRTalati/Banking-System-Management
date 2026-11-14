package com.banking.exceptions;

/**
 * Thrown when any financial operation is attempted on an account that has been frozen.
 */
public class AccountFrozenException extends BankingException {
    private final long accountNumber;

    public AccountFrozenException(long accountNumber) {
        super("ACCOUNT_FROZEN",
                "Account #" + accountNumber + " is FROZEN. All debit, credit, and transfer operations are restricted.");
        this.accountNumber = accountNumber;
    }

    public long getAccountNumber() {
        return accountNumber;
    }
}
