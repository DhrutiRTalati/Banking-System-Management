package com.banking.model;

import java.sql.Timestamp;

/**
 * SavingsAccount implementation extending abstract Account.
 * Demonstrates Polymorphism and specific domain rules for savings products.
 *
 * Rules:
 *  - Minimum balance required: ₹1,000.00
 *  - Earns compoundable annual interest (default 4.0% p.a.)
 */
public class SavingsAccount extends Account {
    public static final double MINIMUM_BALANCE = 1000.0;
    public static final double DEFAULT_INTEREST_RATE = 0.04;

    public SavingsAccount() {
        super();
        setAccountType(AccountType.SAVINGS);
    }

    public SavingsAccount(long accountNumber, int userId, String email, String fullName,
                          double balance, String pinHash, AccountStatus status, Timestamp createdAt) {
        super(accountNumber, userId, email, fullName, balance, pinHash,
                AccountType.SAVINGS, status, createdAt);
    }

    @Override
    public boolean canWithdraw(double amount) {
        if (amount <= 0) return false;
        // Balance after withdrawal must maintain the minimum required balance
        return (getBalance() - amount) >= MINIMUM_BALANCE;
    }

    @Override
    public double calculateInterest(double annualRate) {
        double rate = annualRate > 0 ? annualRate : DEFAULT_INTEREST_RATE;
        // Annual interest on current balance (e.g. monthly interest = annual / 12)
        return (getBalance() * rate) / 12.0;
    }

    @Override
    public String getAccountPolicyDescription() {
        return "Savings Account: Minimum balance ₹1,000.00 required. Earns 4.0% p.a. interest.";
    }
}
