package com.banking.dao;

import com.banking.model.Customer;
import com.banking.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for User and Customer operations.
 * Demonstrates Abstraction, Interfaces, Generics, and Collections (Unit 2 & 4).
 */
public interface UserDAO {

    /**
     * Inserts a new user into the database.
     *
     * @param user the user to persist
     * @return the persisted user with generated ID
     * @throws SQLException on database failure
     */
    User create(User user) throws SQLException;

    /**
     * Retrieves a user by their unique primary key ID.
     */
    Optional<User> findById(int id) throws SQLException;

    /**
     * Retrieves a user by their unique email address.
     */
    Optional<User> findByEmail(String email) throws SQLException;

    /**
     * Updates an existing user's details.
     */
    boolean update(User user) throws SQLException;

    /**
     * Updates the password hash for a specific user ID.
     */
    boolean updatePassword(int userId, String newPasswordHash) throws SQLException;

    /**
     * Returns a list of all registered customers.
     */
    List<Customer> findAllCustomers() throws SQLException;

    /**
     * Searches customers by keyword matching full name, email, or customer code.
     */
    List<Customer> searchCustomers(String query) throws SQLException;

    /**
     * Checks if an email is already registered in the system.
     */
    boolean emailExists(String email) throws SQLException;

    /**
     * Counts the total number of registered customers.
     */
    int countCustomers() throws SQLException;
}
