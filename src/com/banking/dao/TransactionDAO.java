package com.banking.dao;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Financial Transaction Records.
 */
public interface TransactionDAO {

    /**
     * Records a new transaction in the ledger.
     */
    Transaction create(Transaction transaction) throws SQLException;

    /**
     * Records a new transaction within an ongoing shared database connection / transaction.
     */
    Transaction create(Connection conn, Transaction transaction) throws SQLException;

    /**
     * Retrieves a transaction by its primary key ID.
     */
    Optional<Transaction> findById(int id) throws SQLException;

    /**
     * Retrieves transactions for a specific account with a limit, ordered latest first.
     */
    List<Transaction> findByAccountNumber(long accountNumber, int limit) throws SQLException;

    /**
     * Retrieves transactions for an account filtered by transaction type.
     */
    List<Transaction> findByAccountAndType(long accountNumber, TransactionType type, int limit) throws SQLException;

    /**
     * Retrieves all system-wide transactions up to the given limit, ordered latest first.
     */
    List<Transaction> findAll(int limit) throws SQLException;

    /**
     * Counts the total number of transactions recorded in the system.
     */
    int countTotalTransactions() throws SQLException;
}
