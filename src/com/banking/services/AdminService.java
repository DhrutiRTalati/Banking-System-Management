package com.banking.services;

import com.banking.exceptions.BankingException;
import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.SystemStats;
import com.banking.model.Transaction;

import java.util.List;

/**
 * Service interface for administrative monitoring, customer queries,
 * account freeze/unfreeze management, and macro liquidity reports.
 */
public interface AdminService {

    List<Customer> getAllCustomers() throws BankingException;

    List<Customer> searchCustomers(String query) throws BankingException;

    List<Account> getAllAccounts() throws BankingException;

    Account getAccountDetails(long accountNumber) throws BankingException;

    boolean freezeAccount(long accountNumber, String reason) throws BankingException;

    boolean unfreezeAccount(long accountNumber) throws BankingException;

    List<Transaction> getSystemTransactions(int limit) throws BankingException;

    SystemStats getSystemStatistics() throws BankingException;

    int triggerInterestAccrual() throws BankingException;
}
