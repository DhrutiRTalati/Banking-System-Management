package com.banking.services.impl;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.exceptions.AccountFrozenException;
import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.BankingException;
import com.banking.exceptions.InsufficientFundsException;
import com.banking.exceptions.InvalidPinException;
import com.banking.exceptions.ValidationException;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.services.TransactionService;
import com.banking.threads.AsyncAuditLogger;
import com.banking.utilities.NumberGenerator;
import com.banking.utilities.PasswordHasher;
import com.banking.utilities.StatementExporter;
import com.banking.utilities.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of TransactionService managing credits, debits,
 * inter-account transfers, statement queries, and physical statement exports.
 */
public class TransactionServiceImpl implements TransactionService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public TransactionServiceImpl() {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    public TransactionServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public synchronized Transaction deposit(long accountNumber, double amount, String description) throws BankingException {
        ValidationUtil.validatePositiveAmount(amount, "Deposit");

        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }

            Account account = accOpt.get();
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountFrozenException(accountNumber);
            }

            double newBalance = account.getBalance() + amount;
            accountDAO.updateBalance(accountNumber, newBalance);

            Transaction tx = new Transaction(
                    NumberGenerator.generateTransactionReference(),
                    accountNumber,
                    TransactionType.DEPOSIT,
                    amount,
                    description != null && !description.trim().isEmpty() ? description.trim() : "Cash/Online Deposit",
                    newBalance,
                    null
            );
            transactionDAO.create(tx);

            AsyncAuditLogger.getInstance().log("DEPOSIT", account.getEmail(),
                    String.format("Deposited %.2f into Account #%d. New balance: %.2f", amount, accountNumber, newBalance));

            return tx;

        } catch (SQLException e) {
            throw new BankingException("Database error during deposit: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Transaction withdraw(long accountNumber, double amount, String pin, String description)
            throws BankingException {
        ValidationUtil.validatePositiveAmount(amount, "Withdrawal");
        ValidationUtil.validatePin(pin);

        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }

            Account account = accOpt.get();
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountFrozenException(accountNumber);
            }

            // Verify PIN
            if (!PasswordHasher.verify(pin, account.getPinHash())) {
                AsyncAuditLogger.getInstance().log("PIN_FAILED", account.getEmail(),
                        "Failed PIN attempt on withdrawal for Account #" + accountNumber);
                throw new InvalidPinException();
            }

            // Verify withdrawal policy (polymorphic rule)
            if (!account.canWithdraw(amount)) {
                throw new InsufficientFundsException(account.getBalance(), amount);
            }

            double newBalance = account.getBalance() - amount;
            accountDAO.updateBalance(accountNumber, newBalance);

            Transaction tx = new Transaction(
                    NumberGenerator.generateTransactionReference(),
                    accountNumber,
                    TransactionType.WITHDRAWAL,
                    amount,
                    description != null && !description.trim().isEmpty() ? description.trim() : "Cash/ATM Withdrawal",
                    newBalance,
                    null
            );
            transactionDAO.create(tx);

            AsyncAuditLogger.getInstance().log("WITHDRAWAL", account.getEmail(),
                    String.format("Withdrew %.2f from Account #%d. New balance: %.2f", amount, accountNumber, newBalance));

            return tx;

        } catch (SQLException e) {
            throw new BankingException("Database error during withdrawal: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean transfer(long senderAccount, long receiverAccount, double amount,
                                         String pin, String description) throws BankingException {
        if (senderAccount == receiverAccount) {
            throw new ValidationException("Sender and beneficiary account numbers cannot be identical.");
        }
        ValidationUtil.validatePositiveAmount(amount, "Transfer");
        ValidationUtil.validatePin(pin);

        try {
            Optional<Account> senderOpt = accountDAO.findByAccountNumber(senderAccount);
            if (!senderOpt.isPresent()) {
                throw new AccountNotFoundException(senderAccount);
            }
            Account sender = senderOpt.get();

            if (sender.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountFrozenException(senderAccount);
            }

            // Verify PIN
            if (!PasswordHasher.verify(pin, sender.getPinHash())) {
                AsyncAuditLogger.getInstance().log("PIN_FAILED", sender.getEmail(),
                        "Failed PIN attempt during transfer from Account #" + senderAccount);
                throw new InvalidPinException();
            }

            // Verify receiver exists
            Optional<Account> receiverOpt = accountDAO.findByAccountNumber(receiverAccount);
            if (!receiverOpt.isPresent()) {
                throw new AccountNotFoundException("Beneficiary Account #" + receiverAccount);
            }
            Account receiver = receiverOpt.get();

            if (receiver.getStatus() != AccountStatus.ACTIVE) {
                throw new BankingException("Beneficiary Account #" + receiverAccount + " is frozen and cannot receive funds.");
            }

            // Verify balance rule
            if (!sender.canWithdraw(amount)) {
                throw new InsufficientFundsException(sender.getBalance(), amount);
            }

            String senderRef = NumberGenerator.generateTransactionReference();
            String receiverRef = NumberGenerator.generateTransactionReference();
            String desc = (description != null && !description.trim().isEmpty()) ?
                    description.trim() : ("Transfer to #" + receiverAccount);

            boolean success = accountDAO.transferFunds(senderAccount, receiverAccount, amount, senderRef, receiverRef, desc);

            if (success) {
                AsyncAuditLogger.getInstance().log("TRANSFER", sender.getEmail(),
                        String.format("Transferred %.2f from #%d to #%d", amount, senderAccount, receiverAccount));
            }

            return success;

        } catch (SQLException e) {
            throw new BankingException("Transaction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getMiniStatement(long accountNumber) throws BankingException {
        return getTransactionHistory(accountNumber, 5);
    }

    @Override
    public List<Transaction> getTransactionHistory(long accountNumber, int limit) throws BankingException {
        try {
            return transactionDAO.findByAccountNumber(accountNumber, limit);
        } catch (SQLException e) {
            throw new BankingException("Failed to fetch transaction history: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getFilteredTransactions(long accountNumber, TransactionType type, int limit)
            throws BankingException {
        try {
            if (type == null) {
                return transactionDAO.findByAccountNumber(accountNumber, limit);
            }
            return transactionDAO.findByAccountAndType(accountNumber, type, limit);
        } catch (SQLException e) {
            throw new BankingException("Failed to fetch filtered transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exportStatement(long accountNumber, File targetFile, boolean isCsv) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }
            Account account = accOpt.get();
            List<Transaction> transactions = transactionDAO.findByAccountNumber(accountNumber, 500);

            if (isCsv) {
                return StatementExporter.exportToCsvFile(account, transactions, targetFile);
            } else {
                return StatementExporter.exportToTextFile(account, transactions, targetFile);
            }
        } catch (SQLException | IOException e) {
            throw new BankingException("Failed to export statement file: " + e.getMessage(), e);
        }
    }
}
