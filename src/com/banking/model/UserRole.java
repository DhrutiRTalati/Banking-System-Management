package com.banking.model;

/**
 * Enumeration representing user access roles within the banking platform.
 */
public enum UserRole {
    CUSTOMER("Customer"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null) return CUSTOMER;
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(roleStr.trim())) {
                return role;
            }
        }
        return CUSTOMER;
    }
}
