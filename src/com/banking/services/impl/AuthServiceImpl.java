package com.banking.services.impl;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.UserDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.dao.impl.UserDAOImpl;
import com.banking.exceptions.AuthenticationException;
import com.banking.exceptions.BankingException;
import com.banking.exceptions.DuplicateUserException;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.model.CurrentAccount;
import com.banking.model.Customer;
import com.banking.model.SavingsAccount;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.model.User;
import com.banking.services.AuthService;
import com.banking.threads.AsyncAuditLogger;
import com.banking.utilities.NumberGenerator;
import com.banking.utilities.PasswordHasher;
import com.banking.utilities.ValidationUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Concrete implementation of AuthService handling authentication, registration,
 * and credential lifecycles.
 */
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    public AuthServiceImpl(UserDAO userDAO, AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public User login(String email, String password) throws BankingException {
        ValidationUtil.validateEmail(email);
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password cannot be empty.");
        }

        try {
            Optional<User> userOpt = userDAO.findByEmail(email);
            if (!userOpt.isPresent()) {
                AsyncAuditLogger.getInstance().log("LOGIN_FAILED", email, "Email not found");
                throw new AuthenticationException("Invalid email or password.");
            }

            User user = userOpt.get();
            if (!PasswordHasher.verify(password, user.getPasswordHash())) {
                AsyncAuditLogger.getInstance().log("LOGIN_FAILED", email, "Invalid password attempt");
                throw new AuthenticationException("Invalid email or password.");
            }

            AsyncAuditLogger.getInstance().log("LOGIN_SUCCESS", email, "Role: " + user.getRole());
            return user;

        } catch (SQLException e) {
            throw new BankingException("Database error during login: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer registerCustomer(String fullName, String email, String password,
                                     String phone, String address, AccountType accountType,
                                     double initialDeposit, String securityPin) throws BankingException {
        // 1. Validations
        ValidationUtil.validateName(fullName);
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.validatePin(securityPin);

        double minBalance = (accountType == AccountType.CURRENT) ?
                CurrentAccount.MINIMUM_BALANCE : SavingsAccount.MINIMUM_BALANCE;
        if (initialDeposit < minBalance) {
            throw new BankingException(String.format("Initial deposit of %.2f is less than required minimum balance of %.2f for %s",
                    initialDeposit, minBalance, accountType.getDisplayName()));
        }

        try {
            // 2. Check for duplicate user
            if (userDAO.emailExists(email)) {
                throw new DuplicateUserException(email);
            }

            // 3. Create User entity
            String customerCode = NumberGenerator.generateCustomerCode();
            Customer customer = new Customer(
                    fullName.trim(),
                    email.trim().toLowerCase(),
                    PasswordHasher.hash(password),
                    phone != null ? phone.trim() : "",
                    address != null ? address.trim() : ""
            );
            customer.setCustomerCode(customerCode);
            userDAO.create(customer);

            // 4. Create Account entity
            long accountNumber = NumberGenerator.generateAccountNumber();
            Account account;
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (accountType == AccountType.CURRENT) {
                account = new CurrentAccount(accountNumber, customer.getId(), customer.getEmail(),
                        customer.getFullName(), initialDeposit, PasswordHasher.hash(securityPin),
                        AccountStatus.ACTIVE, now);
            } else {
                account = new SavingsAccount(accountNumber, customer.getId(), customer.getEmail(),
                        customer.getFullName(), initialDeposit, PasswordHasher.hash(securityPin),
                        AccountStatus.ACTIVE, now);
            }
            accountDAO.create(account);

            // 5. Create initial deposit transaction record
            if (initialDeposit > 0) {
                Transaction initialTx = new Transaction(
                        NumberGenerator.generateTransactionReference(),
                        accountNumber,
                        TransactionType.DEPOSIT,
                        initialDeposit,
                        "Initial Account Opening Deposit",
                        initialDeposit,
                        null
                );
                transactionDAO.create(initialTx);
            }

            AsyncAuditLogger.getInstance().log("REGISTER_CUSTOMER", email,
                    String.format("Created Customer '%s' with %s Account #%d", fullName, accountType, accountNumber));

            return customer;

        } catch (SQLException e) {
            throw new BankingException("Failed to register customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws BankingException {
        ValidationUtil.validatePassword(newPassword);
        try {
            Optional<User> userOpt = userDAO.findById(userId);
            if (!userOpt.isPresent()) {
                throw new AuthenticationException("User account not found.");
            }
            User user = userOpt.get();
            if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Incorrect existing password.");
            }

            boolean updated = userDAO.updatePassword(userId, PasswordHasher.hash(newPassword));
            if (updated) {
                AsyncAuditLogger.getInstance().log("PASSWORD_CHANGE", user.getEmail(), "Password changed successfully");
            }
            return updated;
        } catch (SQLException e) {
            throw new BankingException("Failed to change password: " + e.getMessage(), e);
        }
    }
}
