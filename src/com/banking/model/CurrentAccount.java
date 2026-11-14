package com.banking.model;

import java.sql.Timestamp;

/**
 * CurrentAccount implementation extending abstract Account.
 * Tailored for commercial and business daily transaction volumes.
 *
 * Rules:
 *  - Minimum balance required: ₹500.00
 *  - 0% interest on daily balances
 *  - Allows higher daily transfer limits
 */
public class CurrentAccount extends Account {
    public static final double MINIMUM_BALANCE = 500.0;

    public CurrentAccount() {
        super();
        setAccountType(AccountType.CURRENT);
    }

    public CurrentAccount(long accountNumber, int userId, String email, String fullName,
                          double balance, String pinHash, AccountStatus status, Timestamp createdAt) {
        super(accountNumber, userId, email, fullName, balance, pinHash,
                AccountType.CURRENT, status, createdAt);
    }

    @Override
    public boolean canWithdraw(double amount) {
        if (amount <= 0) return false;
        return (getBalance() - amount) >= MINIMUM_BALANCE;
    }

    @Override
    public double calculateInterest(double annualRate) {
        // Current accounts typically accrue zero interest in commercial banking
        return 0.0;
    }

    @Override
    public String getAccountPolicyDescription() {
        return "Current Account: Minimum balance ₹500.00. High-volume business transactions, 0% interest.";
    }
}
