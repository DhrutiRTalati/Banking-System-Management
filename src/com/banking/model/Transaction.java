package com.banking.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Transaction entity representing an immutable ledger record of a banking event.
 * Encapsulates all details required for statement generation and passbook viewing.
 */
public class Transaction {
    private int id;
    private String transactionRef;
    private long accountNumber;
    private TransactionType type;
    private double amount;
    private String description;
    private double balanceAfter;
    private Long refAccount; // Counterpart account number for P2P transfers (nullable)
    private Timestamp timestamp;

    public Transaction() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Transaction(int id, String transactionRef, long accountNumber, TransactionType type,
                       double amount, String description, double balanceAfter,
                       Long refAccount, Timestamp timestamp) {
        this.id = id;
        this.transactionRef = transactionRef;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.refAccount = refAccount;
        this.timestamp = timestamp != null ? timestamp : new Timestamp(System.currentTimeMillis());
    }

    public Transaction(String transactionRef, long accountNumber, TransactionType type,
                       double amount, String description, double balanceAfter, Long refAccount) {
        this(0, transactionRef, accountNumber, type, amount, description, balanceAfter,
                refAccount, new Timestamp(System.currentTimeMillis()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Long getRefAccount() {
        return refAccount;
    }

    public void setRefAccount(Long refAccount) {
        this.refAccount = refAccount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "ref='" + transactionRef + '\'' +
                ", acc=" + accountNumber +
                ", type=" + type +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", time=" + getFormattedTimestamp() +
                '}';
    }
}
