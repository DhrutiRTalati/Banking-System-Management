package com.banking.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and manages configuration properties for database connectivity and system settings.
 * Ensures credentials and environment-specific settings are loaded safely outside source code.
 */
public final class ConfigLoader {
    private static final String CONFIG_FILE_PATH = "config.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    private ConfigLoader() {}

    public static synchronized void loadConfig() {
        if (loaded) return;

        // Default baseline configuration: SQLite for zero-setup execution
        properties.setProperty("db.type", "sqlite");
        properties.setProperty("db.sqlite.path", "data/banking.db");
        properties.setProperty("db.mysql.host", "localhost");
        properties.setProperty("db.mysql.port", "3306");
        properties.setProperty("db.mysql.database", "banking_system");
        properties.setProperty("db.mysql.username", "root");
        properties.setProperty("db.mysql.password", "");
        properties.setProperty("bank.name", "Apex Digital Bank");
        properties.setProperty("bank.currency", "INR");
        properties.setProperty("bank.currency.symbol", "₹");

        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("Warning: Could not read config.properties, using defaults: " + e.getMessage());
            }
        } else {
            // Check classpath
            try (InputStream cpStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
                if (cpStream != null) {
                    properties.load(cpStream);
                } else {
                    // Create default config file if it does not exist
                    saveDefaultConfigFile(configFile);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not save default config.properties: " + e.getMessage());
            }
        }

        loaded = true;
    }

    private static void saveDefaultConfigFile(File target) {
        try {
            if (target.getParentFile() != null) {
                target.getParentFile().mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(target)) {
                properties.store(out, "Apex Digital Banking System Configuration\n" +
                        "# Database Type options: sqlite | mysql\n" +
                        "# Note: SQLite requires zero installation and is enabled by default.");
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not write default config.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        if (!loaded) loadConfig();
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (!loaded) loadConfig();
        return properties.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public static int getInt(String key, int defaultValue) {
        String val = getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
