package com.banking.model;

/**
 * Enumeration representing the operational lifecycle status of an account.
 */
public enum AccountStatus {
    ACTIVE("Active"),
    FROZEN("Frozen"),
    CLOSED("Closed");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOperable() {
        return this == ACTIVE;
    }

    public static AccountStatus fromString(String statusStr) {
        if (statusStr == null) return ACTIVE;
        for (AccountStatus status : values()) {
            if (status.name().equalsIgnoreCase(statusStr.trim())) {
                return status;
            }
        }
        return ACTIVE;
    }
}
