package com.banking.model;

import java.sql.Timestamp;

/**
 * Base User entity representing an authenticated identity in the banking platform.
 * Demonstrates Encapsulation with private fields and validated accessors.
 */
public class User {
    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private UserRole role;
    private Timestamp createdAt;

    public User() {
        this.role = UserRole.CUSTOMER;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public User(int id, String fullName, String email, String passwordHash, UserRole role, Timestamp createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.CUSTOMER;
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    public User(String fullName, String email, String passwordHash, UserRole role) {
        this(0, fullName, email, passwordHash, role, new Timestamp(System.currentTimeMillis()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
