-- ==============================================================================
-- Apex Digital Banking System - Database Schema
-- ==============================================================================
-- Note: The application automatically creates these tables upon startup.
-- This script is provided for reference, manual migrations, or DBA reviews.

-- 1. Users Table (Handles Customer and Admin identities)
CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name       TEXT NOT NULL,
    email           TEXT UNIQUE NOT NULL,
    password        TEXT NOT NULL,                       -- SHA-256 Hash
    role            TEXT NOT NULL DEFAULT 'CUSTOMER',    -- 'CUSTOMER' or 'ADMIN'
    phone           TEXT,
    address         TEXT,
    customer_code   TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Accounts Table (Handles Savings and Current accounts)
CREATE TABLE IF NOT EXISTS accounts (
    account_number  INTEGER PRIMARY KEY,
    user_id         INTEGER,
    email           TEXT NOT NULL,
    full_name       TEXT NOT NULL,
    balance         REAL DEFAULT 0.0,
    security_pin    TEXT NOT NULL,                       -- SHA-256 Hash
    account_type    TEXT NOT NULL DEFAULT 'SAVINGS',     -- 'SAVINGS' or 'CURRENT'
    status          TEXT NOT NULL DEFAULT 'ACTIVE',      -- 'ACTIVE', 'FROZEN', 'CLOSED'
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. Transactions Table (Immutable audit ledger of all banking events)
CREATE TABLE IF NOT EXISTS transactions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_ref TEXT UNIQUE NOT NULL,                -- e.g. TXN-20261114-123456
    account_number  INTEGER NOT NULL,
    type            TEXT NOT NULL,                       -- DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST_CREDIT
    amount          REAL NOT NULL,
    description     TEXT,
    balance_after   REAL NOT NULL,
    ref_account     INTEGER,                             -- Counterpart account for P2P transfers
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

-- 4. Audit Logs Table (Tracks security & system events)
CREATE TABLE IF NOT EXISTS audit_logs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    event_type      TEXT NOT NULL,
    actor           TEXT NOT NULL,
    details         TEXT,
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default Admin Credential Seed:
-- Email: admin@bank.com
-- Password: Admin@123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
