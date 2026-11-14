package com.banking.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object Interface for System Audit Trail logs.
 */
public interface AuditLogDAO {
    void log(String eventType, String actor, String details) throws SQLException;
    List<String> getRecentLogs(int limit) throws SQLException;
}
