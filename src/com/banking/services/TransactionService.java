package com.banking.services;

import com.banking.exceptions.BankingException;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.io.File;
import java.util.List;

/**
 * Service interface for financial operations: deposits, withdrawals, transfers,
 * statements, and audit ledgers.
 */
public interface TransactionService {

    /**
     * Credits money into an active bank account.
     */
    Transaction deposit(long accountNumber, double amount, String description) throws BankingException;

    /**
     * Debits money from an active bank account after PIN verification and balance checks.
     */
    Transaction withdraw(long accountNumber, double amount, String pin, String description) throws BankingException;

    /**
     * Executes an inter-account fund transfer with ACID transactional integrity.
     */
    boolean transfer(long senderAccount, long receiverAccount, double amount,
                     String pin, String description) throws BankingException;

    /**
     * Retrieves mini statement (last 5 transactions).
     */
    List<Transaction> getMiniStatement(long accountNumber) throws BankingException;

    /**
     * Retrieves full transaction ledger history up to limit.
     */
    List<Transaction> getTransactionHistory(long accountNumber, int limit) throws BankingException;

    /**
     * Retrieves transactions filtered by type.
     */
    List<Transaction> getFilteredTransactions(long accountNumber, TransactionType type, int limit) throws BankingException;

    /**
     * Exports transactions to a text or CSV file on disk.
     */
    boolean exportStatement(long accountNumber, File targetFile, boolean isCsv) throws BankingException;
}
