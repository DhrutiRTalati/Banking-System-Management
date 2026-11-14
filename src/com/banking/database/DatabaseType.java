package com.banking.database;

public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static DatabaseType fromString(String typeStr) {
        if (typeStr == null) return SQLITE;
        if (typeStr.equalsIgnoreCase("mysql")) return MYSQL;
        return SQLITE;
    }
}
