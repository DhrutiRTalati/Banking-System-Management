package com.banking.dao.impl;

import com.banking.dao.TransactionDAO;
import com.banking.database.DBConnection;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of TransactionDAO.
 * Ensures an immutable financial audit trail.
 */
public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public Transaction create(Transaction tx) throws SQLException {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            return create(conn, tx);
        }
    }

    @Override
    public Transaction create(Connection conn, Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_ref, account_number, type, amount, " +
                "description, balance_after, ref_account, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tx.getTransactionRef());
            stmt.setLong(2, tx.getAccountNumber());
            stmt.setString(3, tx.getType().name());
            stmt.setDouble(4, tx.getAmount());
            stmt.setString(5, tx.getDescription());
            stmt.setDouble(6, tx.getBalanceAfter());
            if (tx.getRefAccount() != null) {
                stmt.setLong(7, tx.getRefAccount());
            } else {
                stmt.setNull(7, java.sql.Types.BIGINT);
            }
            stmt.setTimestamp(8, tx.getTimestamp());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
            return tx;
        }
    }

    @Override
    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByAccountNumber(long accountNumber, int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC, id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountNumber);
            stmt.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> findByAccountAndType(long accountNumber, TransactionType type, int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? AND type = ? ORDER BY timestamp DESC, id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountNumber);
            stmt.setString(2, type.name());
            stmt.setInt(3, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> findAll(int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC, id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int countTotalTransactions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getInt("id"));
        tx.setTransactionRef(rs.getString("transaction_ref"));
        tx.setAccountNumber(rs.getLong("account_number"));
        tx.setType(TransactionType.fromString(rs.getString("type")));
        tx.setAmount(rs.getDouble("amount"));
        tx.setDescription(rs.getString("description"));
        tx.setBalanceAfter(rs.getDouble("balance_after"));
        long ref = rs.getLong("ref_account");
        if (!rs.wasNull()) {
            tx.setRefAccount(ref);
        } else {
            tx.setRefAccount(null);
        }
        tx.setTimestamp(rs.getTimestamp("timestamp"));
        return tx;
    }
}
