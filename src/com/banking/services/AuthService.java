package com.banking.services;

import com.banking.exceptions.BankingException;
import com.banking.model.AccountType;
import com.banking.model.Customer;
import com.banking.model.User;

/**
 * Service interface for User Registration, Authentication, and Credential Management.
 * Demonstrates Abstraction and Layered Architecture (Unit 2).
 */
public interface AuthService {

    /**
     * Authenticates a user with email and password.
     *
     * @param email    user email
     * @param password raw password
     * @return the authenticated User (Customer or Admin)
     * @throws BankingException if authentication fails
     */
    User login(String email, String password) throws BankingException;

    /**
     * Registers a new customer and opens their first bank account simultaneously.
     *
     * @param fullName       customer full name
     * @param email          customer email address
     * @param password       raw password
     * @param phone          phone number
     * @param address        physical address
     * @param accountType    SAVINGS or CURRENT
     * @param initialDeposit starting balance
     * @param securityPin    4-digit transaction PIN
     * @return the newly registered Customer
     * @throws BankingException on validation or persistence error
     */
    Customer registerCustomer(String fullName, String email, String password,
                              String phone, String address, AccountType accountType,
                              double initialDeposit, String securityPin) throws BankingException;

    /**
     * Modifies an existing user's password.
     */
    boolean changePassword(int userId, String oldPassword, String newPassword) throws BankingException;
}
