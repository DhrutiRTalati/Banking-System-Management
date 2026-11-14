package com.banking.services.impl;

import com.banking.dao.AccountDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.BankingException;
import com.banking.exceptions.InvalidPinException;
import com.banking.model.Account;
import com.banking.services.AccountService;
import com.banking.threads.AsyncAuditLogger;
import com.banking.utilities.PasswordHasher;
import com.banking.utilities.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Concrete implementation of AccountService.
 */
public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;

    public AccountServiceImpl() {
        this.accountDAO = new AccountDAOImpl();
    }

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account getAccountByNumber(long accountNumber) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByAccountNumber(accountNumber);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(accountNumber);
            }
            return accOpt.get();
        } catch (SQLException e) {
            throw new BankingException("Database error loading account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccountByUserId(int userId) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByUserId(userId);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException("User ID #" + userId);
            }
            return accOpt.get();
        } catch (SQLException e) {
            throw new BankingException("Database error loading account by user: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccountByEmail(String email) throws BankingException {
        try {
            Optional<Account> accOpt = accountDAO.findByEmail(email);
            if (!accOpt.isPresent()) {
                throw new AccountNotFoundException(email);
            }
            return accOpt.get();
        } catch (SQLException e) {
            throw new BankingException("Database error loading account by email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPin(long accountNumber, String pin) throws BankingException {
        ValidationUtil.validatePin(pin);
        Account account = getAccountByNumber(accountNumber);
        boolean valid = PasswordHasher.verify(pin, account.getPinHash());
        if (!valid) {
            AsyncAuditLogger.getInstance().log("PIN_FAILED", account.getEmail(),
                    "Incorrect PIN attempt on Account #" + accountNumber);
            throw new InvalidPinException();
        }
        return true;
    }

    @Override
    public boolean updatePin(long accountNumber, String oldPin, String newPin) throws BankingException {
        ValidationUtil.validatePin(newPin);
        verifyPin(accountNumber, oldPin); // verifies old pin or throws InvalidPinException

        try {
            boolean updated = accountDAO.updatePin(accountNumber, PasswordHasher.hash(newPin));
            if (updated) {
                Account account = getAccountByNumber(accountNumber);
                AsyncAuditLogger.getInstance().log("PIN_CHANGE", account.getEmail(),
                        "PIN successfully updated for Account #" + accountNumber);
            }
            return updated;
        } catch (SQLException e) {
            throw new BankingException("Failed to update PIN: " + e.getMessage(), e);
        }
    }
}
