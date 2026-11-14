package com.banking.dao;

import com.banking.model.Account;
import com.banking.model.AccountStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Bank Accounts.
 * Supports retrieval, updates, status changes, and atomic transactional fund transfers.
 */
public interface AccountDAO {

    /**
     * Persists a new bank account.
     */
    Account create(Account account) throws SQLException;

    /**
     * Finds an account by account number.
     */
    Optional<Account> findByAccountNumber(long accountNumber) throws SQLException;

    /**
     * Finds an account by user ID.
     */
    Optional<Account> findByUserId(int userId) throws SQLException;

    /**
     * Finds an account by user email.
     */
    Optional<Account> findByEmail(String email) throws SQLException;

    /**
     * Updates an account's balance directly.
     */
    boolean updateBalance(long accountNumber, double newBalance) throws SQLException;

    /**
     * Updates an account's operational status (ACTIVE, FROZEN, CLOSED).
     */
    boolean updateStatus(long accountNumber, AccountStatus status) throws SQLException;

    /**
     * Updates the security PIN hash for an account.
     */
    boolean updatePin(long accountNumber, String newPinHash) throws SQLException;

    /**
     * Retrieves all accounts in the system.
     */
    List<Account> findAll() throws SQLException;

    /**
     * Counts accounts by their operational status.
     */
    int countByStatus(AccountStatus status) throws SQLException;

    /**
     * Computes the sum of all balances across all active accounts (Total Liquidity).
     */
    double getTotalLiquidity() throws SQLException;

    /**
     * Executes an atomic ACID fund transfer between two accounts within a single transaction.
     * Guaranteed rollback if any step fails.
     */
    boolean transferFunds(long senderAccount, long receiverAccount, double amount,
                          String senderRef, String receiverRef,
                          String description) throws SQLException;
}
