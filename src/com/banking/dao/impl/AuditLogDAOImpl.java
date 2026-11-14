package com.banking.dao.impl;

import com.banking.dao.AuditLogDAO;
import com.banking.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of AuditLogDAO for persistent tracking of system actions.
 */
public class AuditLogDAOImpl implements AuditLogDAO {

    @Override
    public void log(String eventType, String actor, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (event_type, actor, details) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventType);
            stmt.setString(2, actor);
            stmt.setString(3, details);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> getRecentLogs(int limit) throws SQLException {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT timestamp, event_type, actor, details FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String line = String.format("[%s] %s | By: %s | %s",
                            rs.getString("timestamp"),
                            rs.getString("event_type"),
                            rs.getString("actor"),
                            rs.getString("details"));
                    logs.add(line);
                }
            }
        }
        return logs;
    }
}
