package com.banking.services;

import com.banking.exceptions.BankingException;
import com.banking.model.Account;

/**
 * Service interface for Account lifecycle and PIN security management.
 */
public interface AccountService {

    /**
     * Retrieves account by account number.
     */
    Account getAccountByNumber(long accountNumber) throws BankingException;

    /**
     * Retrieves account by user ID.
     */
    Account getAccountByUserId(int userId) throws BankingException;

    /**
     * Retrieves account by email.
     */
    Account getAccountByEmail(String email) throws BankingException;

    /**
     * Verifies whether the provided PIN matches the account's security PIN.
     */
    boolean verifyPin(long accountNumber, String pin) throws BankingException;

    /**
     * Changes an account's security PIN.
     */
    boolean updatePin(long accountNumber, String oldPin, String newPin) throws BankingException;
}
