package com.banking.model;

import com.banking.exceptions.AccountFrozenException;
import com.banking.exceptions.InsufficientFundsException;

import java.sql.Timestamp;

/**
 * Abstract base class for all bank accounts.
 * Demonstrates Abstraction, Polymorphism (polymorphic withdrawal rules and interest calculation),
 * and Encapsulation.
 */
public abstract class Account {
    private long accountNumber;
    private int userId;
    private String email;
    private String fullName;
    private double balance;
    private String pinHash;
    private AccountType accountType;
    private AccountStatus status;
    private Timestamp createdAt;

    public Account() {
        this.status = AccountStatus.ACTIVE;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Account(long accountNumber, int userId, String email, String fullName,
                   double balance, String pinHash, AccountType accountType,
                   AccountStatus status, Timestamp createdAt) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.balance = balance;
        this.pinHash = pinHash;
        this.accountType = accountType;
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    // --- Abstract Polymorphic Methods ---
    /**
     * Checks if a withdrawal of the given amount is permissible under this account's rules.
     *
     * @param amount the requested withdrawal amount
     * @return true if permitted, false otherwise
     */
    public abstract boolean canWithdraw(double amount);

    /**
     * Calculates the interest accrued for this account given an annual interest rate.
     *
     * @param annualRate the annual interest rate as a fraction (e.g. 0.04 for 4%)
     * @return calculated interest amount
     */
    public abstract double calculateInterest(double annualRate);

    /**
     * Returns a human-readable description of this account type's rules and policies.
     */
    public abstract String getAccountPolicyDescription();

    // --- Concrete Business Logic Methods ---

    /**
     * Deducts an amount from the balance after validating state and balance rules.
     */
    public synchronized void debit(double amount) throws AccountFrozenException, InsufficientFundsException {
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountNumber);
        }
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException(balance, amount);
        }
        this.balance -= amount;
    }

    /**
     * Adds an amount to the balance after validating active status.
     */
    public synchronized void credit(double amount) throws AccountFrozenException {
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountNumber);
        }
        this.balance += amount;
    }

    /**
     * Returns a masked representation of the balance (e.g. ₹ ••••••••) for privacy.
     */
    public String getMaskedBalance() {
        return "₹ ••••••••";
    }

    // --- Getters & Setters (Encapsulation) ---

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber=" + accountNumber +
                ", fullName='" + fullName + '\'' +
                ", balance=" + balance +
                ", type=" + accountType +
                ", status=" + status +
                '}';
    }
}
