package com.banking.model;

import java.sql.Timestamp;

/**
 * Customer entity extending User.
 * Demonstrates Inheritance and constructor chaining via super().
 */
public class Customer extends User {
    private String phoneNumber;
    private String address;
    private String customerCode;

    public Customer() {
        super();
        setRole(UserRole.CUSTOMER);
    }

    public Customer(int id, String fullName, String email, String passwordHash,
                    String phoneNumber, String address, String customerCode, Timestamp createdAt) {
        super(id, fullName, email, passwordHash, UserRole.CUSTOMER, createdAt);
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.customerCode = customerCode;
    }

    public Customer(String fullName, String email, String passwordHash,
                    String phoneNumber, String address) {
        super(fullName, email, passwordHash, UserRole.CUSTOMER);
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + phoneNumber + '\'' +
                ", code='" + customerCode + '\'' +
                '}';
    }
}
