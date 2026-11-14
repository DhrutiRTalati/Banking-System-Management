package com.banking.services.impl;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.UserDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.dao.impl.UserDAOImpl;
import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.BankingException;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.Customer;
import com.banking.model.SystemStats;
import com.banking.model.Transaction;
import com.banking.services.AdminService;
import com.banking.threads.AsyncAuditLogger;
import com.banking.threads.BackgroundInterestWorker;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of AdminService for managerial oversight,
 * risk control, and system ledger monitoring.
 */
public class AdminServiceImpl implements AdminService {

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AdminServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    public AdminServiceImpl(UserDAO userDAO, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public List<Customer> getAllCustomers() throws BankingException {
        try {
            return userDAO.findAllCustomers();
        } catch (SQLException e) {
            throw new BankingException("Failed to load customer directory: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> searchCustomers(String query) throws BankingException {
        try {
            return userDAO.searchCustomers(query);
        } catch (SQLException e) {
            throw new BankingException("Failed to execute customer search: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getAllAccounts() throws BankingException {
        try {
            return accountDAO.findAll();
        } catch (SQLException e) {
            throw new BankingException("Failed to load account ledger: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccountDetails(long accountNumber) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }
            return accOpt.get();
        } catch (SQLException e) {
            throw new BankingException("Failed to retrieve account #" + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean freezeAccount(long accountNumber, String reason) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }

            boolean updated = accountDAO.updateStatus(accountNumber, AccountStatus.FROZEN);
            if (updated) {
                AsyncAuditLogger.getInstance().log("ACCOUNT_FROZEN", "ADMIN",
                        String.format("Account #%d frozen. Reason: %s", accountNumber,
                                (reason != null && !reason.trim().isEmpty() ? reason : "Administrative precaution")));
            }
            return updated;
        } catch (SQLException e) {
            throw new BankingException("Failed to freeze account #" + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean unfreezeAccount(long accountNumber) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }

            boolean updated = accountDAO.updateStatus(accountNumber, AccountStatus.ACTIVE);
            if (updated) {
                AsyncAuditLogger.getInstance().log("ACCOUNT_UNFROZEN", "ADMIN",
                        String.format("Account #%d restored to ACTIVE status.", accountNumber));
            }
            return updated;
        } catch (SQLException e) {
            throw new BankingException("Failed to unfreeze account #" + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getSystemTransactions(int limit) throws BankingException {
        try {
            return transactionDAO.findAll(limit);
        } catch (SQLException e) {
            throw new BankingException("Failed to load system transaction audit: " + e.getMessage(), e);
        }
    }

    @Override
    public SystemStats getSystemStatistics() throws BankingException {
        try {
            int customers = userDAO.countCustomers();
            int totalAccounts = accountDAO.findAll().size();
            int activeAccounts = accountDAO.countByStatus(AccountStatus.ACTIVE);
            int frozenAccounts = accountDAO.countByStatus(AccountStatus.FROZEN);
            double liquidity = accountDAO.getTotalLiquidity();
            int transactions = transactionDAO.countTotalTransactions();

            return new SystemStats(customers, totalAccounts, activeAccounts,
                    frozenAccounts, liquidity, transactions);
        } catch (SQLException e) {
            throw new BankingException("Failed to calculate macro statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public int triggerInterestAccrual() throws BankingException {
        BackgroundInterestWorker worker = new BackgroundInterestWorker();
        return worker.processInterestAccrual();
    }
}
