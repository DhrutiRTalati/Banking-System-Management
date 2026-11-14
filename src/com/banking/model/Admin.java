package com.banking.model;

import java.sql.Timestamp;

/**
 * Admin entity extending User.
 * Demonstrates Inheritance and role specialization.
 */
public class Admin extends User {
    private String department;
    private int accessLevel;

    public Admin() {
        super();
        setRole(UserRole.ADMIN);
        this.department = "Operations";
        this.accessLevel = 1;
    }

    public Admin(int id, String fullName, String email, String passwordHash,
                 String department, int accessLevel, Timestamp createdAt) {
        super(id, fullName, email, passwordHash, UserRole.ADMIN, createdAt);
        this.department = department;
        this.accessLevel = accessLevel;
    }

    public Admin(String fullName, String email, String passwordHash, String department) {
        super(fullName, email, passwordHash, UserRole.ADMIN);
        this.department = department;
        this.accessLevel = 1;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}
