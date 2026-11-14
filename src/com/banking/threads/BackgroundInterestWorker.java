package com.banking.threads;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.model.SavingsAccount;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.utilities.NumberGenerator;

import java.util.List;

/**
 * Demonstrates Multithreading and Synchronization (Unit 4 & 5).
 * A background worker thread that simulates automated periodic interest accrual
 * for all active Savings Accounts.
 */
public class BackgroundInterestWorker implements Runnable {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final long intervalMillis;
    private volatile boolean running = true;

    public BackgroundInterestWorker(long intervalMillis) {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        this.intervalMillis = intervalMillis;
    }

    public BackgroundInterestWorker() {
        this(600_000L); // 10 minutes default interval
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalMillis);
                processInterestAccrual();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Background interest processing error: " + e.getMessage());
            }
        }
    }

    /**
     * Synchronized method to iterate through all active savings accounts and apply interest.
     */
    public synchronized int processInterestAccrual() {
        int creditedCount = 0;
        try {
            List<Account> accounts = accountDAO.findAll();
            for (Account acc : accounts) {
                if (acc.getAccountType() == AccountType.SAVINGS && acc.getStatus() == AccountStatus.ACTIVE) {
                    double interest = acc.calculateInterest(SavingsAccount.DEFAULT_INTEREST_RATE);
                    if (interest >= 0.01) {
                        double newBalance = acc.getBalance() + interest;
                        boolean updated = accountDAO.updateBalance(acc.getAccountNumber(), newBalance);
                        if (updated) {
                            Transaction tx = new Transaction(
                                    NumberGenerator.generateTransactionReference(),
                                    acc.getAccountNumber(),
                                    TransactionType.INTEREST_CREDIT,
                                    interest,
                                    String.format("Monthly Interest Credit (4.0%% p.a.)"),
                                    newBalance,
                                    null
                            );
                            transactionDAO.create(tx);
                            AsyncAuditLogger.getInstance().log("INTEREST_CREDIT", "SYSTEM",
                                    String.format("Credited %.2f interest to Account #%d", interest, acc.getAccountNumber()));
                            creditedCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating interest: " + e.getMessage());
        }
        return creditedCount;
    }

    public void stop() {
        this.running = false;
    }
}
