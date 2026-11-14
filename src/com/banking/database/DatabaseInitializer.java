package com.banking.database;

import com.banking.utilities.ConfigLoader;
import com.banking.utilities.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automates schema creation and initial administrative data seeding.
 * Ensures the database tables are ready before application startup.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    /**
     * Initializes tables and seeds the default admin user.
     */
    public static void initialize() throws SQLException {
        DBConnection dbConnection = DBConnection.getInstance();
        DatabaseType type = dbConnection.getDatabaseType();

        try (Connection conn = dbConnection.getConnection()) {
            if (type == DatabaseType.SQLITE) {
                initSQLite(conn);
            } else {
                initMySQL(conn);
            }
            seedDefaultAdmin(conn);
        }
    }

    private static void initSQLite(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "full_name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'CUSTOMER', " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "customer_code TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // Accounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number INTEGER PRIMARY KEY, " +
                    "user_id INTEGER, " +
                    "email TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "balance REAL DEFAULT 0.0, " +
                    "security_pin TEXT NOT NULL, " +
                    "account_type TEXT NOT NULL DEFAULT 'SAVINGS', " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ");");

            // Transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "transaction_ref TEXT UNIQUE NOT NULL, " +
                    "account_number INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "description TEXT, " +
                    "balance_after REAL NOT NULL, " +
                    "ref_account INTEGER, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                    ");");

            // Audit logs table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "event_type TEXT NOT NULL, " +
                    "actor TEXT NOT NULL, " +
                    "details TEXT, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");
        }
    }

    private static void initMySQL(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(64) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', " +
                    "phone VARCHAR(20), " +
                    "address VARCHAR(255), " +
                    "customer_code VARCHAR(50), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;");

            // Accounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number BIGINT PRIMARY KEY, " +
                    "user_id INT, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "balance DECIMAL(15, 2) DEFAULT 0.00, " +
                    "security_pin VARCHAR(64) NOT NULL, " +
                    "account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS', " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ") ENGINE=InnoDB;");

            // Transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "transaction_ref VARCHAR(50) UNIQUE NOT NULL, " +
                    "account_number BIGINT NOT NULL, " +
                    "type VARCHAR(30) NOT NULL, " +
                    "amount DECIMAL(15, 2) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "balance_after DECIMAL(15, 2) NOT NULL, " +
                    "ref_account BIGINT, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                    ") ENGINE=InnoDB;");

            // Audit logs table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "event_type VARCHAR(50) NOT NULL, " +
                    "actor VARCHAR(100) NOT NULL, " +
                    "details VARCHAR(500), " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;");
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String adminEmail = ConfigLoader.getProperty("security.default.admin.email", "admin@bank.com");
        String adminPass = ConfigLoader.getProperty("security.default.admin.password", "Admin@123");

        String checkSql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, adminEmail);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    String insertSql = "INSERT INTO users (full_name, email, password, role, phone, address, customer_code) " +
                            "VALUES (?, ?, ?, 'ADMIN', '1800-BANK-ADMIN', 'Apex Central Headquarters', 'ADM-001')";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, "Apex System Administrator");
                        insertStmt.setString(2, adminEmail);
                        insertStmt.setString(3, PasswordHasher.hash(adminPass));
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
