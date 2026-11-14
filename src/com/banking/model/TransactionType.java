package com.banking.model;

/**
 * Enumeration representing all supported financial transaction types.
 */
public enum TransactionType {
    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TRANSFER_IN("Transfer In", true),
    TRANSFER_OUT("Transfer Out", false),
    INTEREST_CREDIT("Interest Credit", true);

    private final String description;
    private final boolean isCredit;

    TransactionType(String description, boolean isCredit) {
        this.description = description;
        this.isCredit = isCredit;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public static TransactionType fromString(String typeStr) {
        if (typeStr == null) return DEPOSIT;
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(typeStr.trim())) {
                return type;
            }
        }
        return DEPOSIT;
    }
}
