package com.banking.database;

import com.banking.utilities.ConfigLoader;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thread-safe Database Connection Manager.
 * Implements the Singleton pattern for JDBC connectivity.
 * Supports SQLite (default, zero-setup embedded file database) and MySQL.
 */
public final class DBConnection {

    private static volatile DBConnection instance;
    private DatabaseType databaseType;
    private String jdbcUrl;
    private String username;
    private String password;

    private DBConnection() {
        initConfiguration();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    private void initConfiguration() {
        ConfigLoader.loadConfig();
        String typeStr = ConfigLoader.getProperty("db.type", "sqlite");
        this.databaseType = DatabaseType.fromString(typeStr);

        if (this.databaseType == DatabaseType.SQLITE) {
            String sqlitePath = ConfigLoader.getProperty("db.sqlite.path", "data/banking.db");
            File dbFile = new File(sqlitePath);
            if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            this.jdbcUrl = "jdbc:sqlite:" + sqlitePath;
            this.username = null;
            this.password = null;
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: SQLite JDBC Driver not found on classpath: " + e.getMessage());
            }
        } else {
            String host = ConfigLoader.getProperty("db.mysql.host", "localhost");
            int port = ConfigLoader.getInt("db.mysql.port", 3306);
            String db = ConfigLoader.getProperty("db.mysql.database", "banking_system");
            this.jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, db);
            this.username = ConfigLoader.getProperty("db.mysql.username", "root");
            this.password = ConfigLoader.getProperty("db.mysql.password", "");
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: MySQL JDBC Driver not found on classpath: " + e.getMessage());
            }
        }
    }

    /**
     * Obtains a new JDBC connection from the driver manager.
     */
    public Connection getConnection() throws SQLException {
        if (databaseType == DatabaseType.SQLITE) {
            Connection conn = DriverManager.getConnection(jdbcUrl);
            // Enable foreign key constraints in SQLite
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            } catch (SQLException ignored) {}
            return conn;
        } else {
            return DriverManager.getConnection(jdbcUrl, username, password);
        }
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Resets instance and reloads configuration (useful if settings change at runtime).
     */
    public static synchronized void reset() {
        instance = null;
    }
}
