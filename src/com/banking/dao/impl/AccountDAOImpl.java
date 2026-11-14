package com.banking.dao.impl;

import com.banking.dao.AccountDAO;
import com.banking.database.DBConnection;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.model.CurrentAccount;
import com.banking.model.SavingsAccount;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of AccountDAO.
 * Features polymorphic account instantiation and ACID atomic transfers.
 */
public class AccountDAOImpl implements AccountDAO {

    @Override
    public Account create(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, user_id, email, full_name, balance, " +
                "security_pin, account_type, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, account.getAccountNumber());
            stmt.setInt(2, account.getUserId());
            stmt.setString(3, account.getEmail());
            stmt.setString(4, account.getFullName());
            stmt.setDouble(5, account.getBalance());
            stmt.setString(6, account.getPinHash());
            stmt.setString(7, account.getAccountType().name());
            stmt.setString(8, account.getStatus().name());
            stmt.setTimestamp(9, account.getCreatedAt());

            stmt.executeUpdate();
            return account;
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(long accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByEmail(String email) throws SQLException {
        if (email == null) return Optional.empty();
        String sql = "SELECT * FROM accounts WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateBalance(long accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setLong(2, accountNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(long accountNumber, AccountStatus status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, accountNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePin(long accountNumber, String newPinHash) throws SQLException {
        String sql = "UPDATE accounts SET security_pin = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPinHash);
            stmt.setLong(2, accountNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAccount(rs));
            }
        }
        return list;
    }

    @Override
    public int countByStatus(AccountStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE status = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public double getTotalLiquidity() throws SQLException {
        String sql = "SELECT SUM(balance) FROM accounts WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public boolean transferFunds(long senderAccount, long receiverAccount, double amount,
                                 String senderRef, String receiverRef,
                                 String description) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 1. Fetch and Lock sender account (or verify sender exists, active, sufficient funds)
            double senderBalance;
            AccountStatus senderStatus;
            AccountType senderType;
            String senderSql = "SELECT balance, status, account_type FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(senderSql)) {
                stmt.setLong(1, senderAccount);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new SQLException("Sender account not found.");
                    }
                    senderBalance = rs.getDouble("balance");
                    senderStatus = AccountStatus.fromString(rs.getString("status"));
                    senderType = AccountType.fromString(rs.getString("account_type"));
                }
            }

            if (senderStatus != AccountStatus.ACTIVE) {
                conn.rollback();
                throw new SQLException("Sender account is not active.");
            }

            double requiredMin = (senderType == AccountType.SAVINGS) ?
                    SavingsAccount.MINIMUM_BALANCE : CurrentAccount.MINIMUM_BALANCE;
            if ((senderBalance - amount) < requiredMin) {
                conn.rollback();
                throw new SQLException(String.format("Transfer exceeds available funds. Balance: %.2f, Min Balance: %.2f",
                        senderBalance, requiredMin));
            }

            // 2. Fetch and verify receiver account
            double receiverBalance;
            AccountStatus receiverStatus;
            String receiverSql = "SELECT balance, status FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(receiverSql)) {
                stmt.setLong(1, receiverAccount);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new SQLException("Beneficiary account not found.");
                    }
                    receiverBalance = rs.getDouble("balance");
                    receiverStatus = AccountStatus.fromString(rs.getString("status"));
                }
            }

            if (receiverStatus != AccountStatus.ACTIVE) {
                conn.rollback();
                throw new SQLException("Beneficiary account is currently frozen or inactive.");
            }

            // 3. Update balances
            double newSenderBalance = senderBalance - amount;
            double newReceiverBalance = receiverBalance + amount;

            String updateSenderSql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSenderSql)) {
                stmt.setDouble(1, newSenderBalance);
                stmt.setLong(2, senderAccount);
                stmt.executeUpdate();
            }

            String updateReceiverSql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateReceiverSql)) {
                stmt.setDouble(1, newReceiverBalance);
                stmt.setLong(2, receiverAccount);
                stmt.executeUpdate();
            }

            // 4. Record ledger transaction for sender (TRANSFER_OUT)
            String insertTxSql = "INSERT INTO transactions (transaction_ref, account_number, type, amount, " +
                    "description, balance_after, ref_account) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTxSql)) {
                stmt.setString(1, senderRef);
                stmt.setLong(2, senderAccount);
                stmt.setString(3, TransactionType.TRANSFER_OUT.name());
                stmt.setDouble(4, amount);
                stmt.setString(5, description != null ? description : "Fund Transfer Out to #" + receiverAccount);
                stmt.setDouble(6, newSenderBalance);
                stmt.setLong(7, receiverAccount);
                stmt.executeUpdate();
            }

            // 5. Record ledger transaction for receiver (TRANSFER_IN)
            try (PreparedStatement stmt = conn.prepareStatement(insertTxSql)) {
                stmt.setString(1, receiverRef);
                stmt.setLong(2, receiverAccount);
                stmt.setString(3, TransactionType.TRANSFER_IN.name());
                stmt.setDouble(4, amount);
                stmt.setString(5, "Fund Transfer In from #" + senderAccount);
                stmt.setDouble(6, newReceiverBalance);
                stmt.setLong(7, senderAccount);
                stmt.executeUpdate();
            }

            conn.commit(); // Commit ACID Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        AccountType type = AccountType.fromString(rs.getString("account_type"));
        AccountStatus status = AccountStatus.fromString(rs.getString("status"));
        long accNum = rs.getLong("account_number");
        int userId = rs.getInt("user_id");
        String email = rs.getString("email");
        String fullName = rs.getString("full_name");
        double balance = rs.getDouble("balance");
        String pinHash = rs.getString("security_pin");
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");

        // Polymorphic object instantiation based on account_type
        if (type == AccountType.CURRENT) {
            return new CurrentAccount(accNum, userId, email, fullName, balance, pinHash, status, createdAt);
        } else {
            return new SavingsAccount(accNum, userId, email, fullName, balance, pinHash, status, createdAt);
        }
    }
}
