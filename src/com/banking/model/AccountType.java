package com.banking.model;

/**
 * Enumeration representing bank account variants supported by the core engine.
 */
public enum AccountType {
    SAVINGS("Savings Account", 1000.0, 0.04), // 1000 min balance, 4.0% interest
    CURRENT("Current Account", 500.0, 0.00);  // 500 min balance, 0% interest

    private final String displayName;
    private final double minimumBalance;
    private final double annualInterestRate;

    AccountType(String displayName, double minimumBalance, double annualInterestRate) {
        this.displayName = displayName;
        this.minimumBalance = minimumBalance;
        this.annualInterestRate = annualInterestRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public static AccountType fromString(String typeStr) {
        if (typeStr == null) return SAVINGS;
        for (AccountType type : values()) {
            if (type.name().equalsIgnoreCase(typeStr.trim()) ||
                type.displayName.equalsIgnoreCase(typeStr.trim())) {
                return type;
            }
        }
        return SAVINGS;
    }
}
