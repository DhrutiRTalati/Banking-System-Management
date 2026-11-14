# 🏦 Apex Digital Banking System (ADBS)

[![Java](https://img.shields.io/badge/Java-21%2B%20%2F%2027%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![GUI](https://img.shields.io/badge/GUI-Java%20Swing-5382a1?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Database](https://img.shields.io/badge/Database-SQLite%20%7C%20MySQL-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Security](https://img.shields.io/badge/Security-SHA--256%20Cryptographic%20Hashing-green?style=for-the-badge)](https://en.wikipedia.org/wiki/SHA-2)
[![Architecture](https://img.shields.io/badge/Architecture-Layered%20Service--DAO-blueviolet?style=for-the-badge)](https://en.wikipedia.org/wiki/Data_access_object)

An enterprise-grade, production-quality Digital Banking Management System engineered from scratch in **Core Java**. The application strictly implements **Object-Oriented Programming (OOP)**, **Layered Architecture (Model-DAO-Service-GUI-Threads-Utilities)**, **ACID Database Transactions**, and **Cryptographic Data Protection**.

The platform provides both a modern, navy-blue and gold themed **Java Swing Desktop Graphical User Interface (GUI)** as well as an interactive **Command-Line Interface (CLI)** sharing the exact same DAO and Service engine.

---

## 📑 Table of Contents
1. [System Architecture](#-system-architecture)
2. [Key Features & Capabilities](#-key-features--capabilities)
3. [Comprehensive Java Concept Mapping (Units 1 - 5)](#-comprehensive-java-concept-mapping-units-1---5)
4. [Database & ACID Transaction Consistency](#-database--acid-transaction-consistency)
5. [Project Directory & Package Structure](#-project-directory--package-structure)
6. [Compilation & Execution Instructions](#-compilation--execution-instructions)
7. [Default Evaluation Credentials & Demo Guide](#-default-evaluation-credentials--demo-guide)
8. [Student Viva & Technical Defense Guide](#-student-viva--technical-defense-guide)

---

## 🏛️ System Architecture

The application adheres to clean separation of concerns across 8 distinct architectural packages:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   • Java Swing GUI (MainFrame, Panels, Dialogs, Theme)  │
│   • Command-Line Console (BankingCLI)                  │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                     Service Layer                      │
│   • AuthService, AccountService, TransactionService     │
│   • AdminService                                       │
│   (Business validations, rules, policy orchestration)  │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                Data Access Object (DAO)                │
│   • UserDAO, AccountDAO, TransactionDAO, AuditLogDAO   │
│   (Pure PreparedStatements, SQL injection immunity)    │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                 Database & Storage                     │
│   • SQLite Embedded (Default zero-config: banking.db)  │
│   • MySQL Support (via config.properties)              │
│   • File I/O: logs/banking_audit.log, exports/*.txt    │
└────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features & Capabilities

### 👤 1. Customer Banking Portal
- **Self-Service Onboarding & Account Opening**: Clean multi-field registration with strict regex email validation, phone validation, password requirements, initial deposit validation, and 4-digit security PIN setup.
- **Account Number Generation**: Cryptographically secure 10-digit account numbers generated via `NumberGenerator.java`.
- **Account Variants (`SAVINGS` vs `CURRENT`)**:
  - **Savings Account**: Minimum balance of ₹1,000.00 strictly enforced; earns 4.0% p.a. interest.
  - **Current Account**: Minimum balance of ₹500.00; zero interest; tailored for commercial transactions.
- **Privacy Balance Masking (`₹ ••••••••`)**: By default, account balances are masked on the dashboard to protect privacy in public spaces. A toggle button allows the customer to instantly reveal or re-mask the balance.
- **Cash / Online Deposits**: Instant credit to balance with automatic audit logging and transaction reference generation.
- **Cash / ATM Withdrawals**: Guarded by 4-digit PIN verification and polymorphic minimum balance enforcement (`canWithdraw`).
- **Inter-Bank & P2P Fund Transfers**: Guaranteed atomic execution (`connection.setAutoCommit(false)` with rollback on error); includes beneficiary pre-verification to prevent accidental transfers.
- **Mini-Statement**: Quick-view modal displaying the last 5 transactions with running balances.
- **Passbook & Transaction Ledger**: Responsive table view with color-coded badges (+CREDIT in emerald green, -DEBIT in crimson red) and type filtering.
- **Statement File Export**: One-click export to formatted `.txt` statements and `.csv` spreadsheets for record keeping.
- **Security PIN Management**: Secure self-service 4-digit PIN update.

### 🛡️ 2. Administrative Control Console
- **Default Seeded Admin**: Auto-seeded on first run (`admin@bank.com` / `Admin@123`).
- **Macro System KPI Metrics**: Real-time cards showing Total Customers, Total Accounts, Active Accounts, Frozen Accounts, Total Liquidity (bank deposits), and Total Transactions.
- **Customer Directory & Live Search**: Filter registered customers by Name, Email, Phone, or Customer Code.
- **Account Risk & Fraud Control (Freeze / Unfreeze)**: One-click locking of compromised accounts with justification note; immediately blocks all debit, credit, and transfer operations.
- **System-Wide Audit Ledger**: Complete chronological feed of every transaction executed across the entire banking platform.
- **Simulate Interest Accrual**: Triggers background calculation worker to credit monthly interest on active savings accounts.

---

## 📚 Comprehensive Java Concept Mapping (Units 1 - 5)

This codebase is intentionally designed to demonstrate concepts from all five core Java syllabus units:

### Unit 1: OOP Foundations & Class Hierarchies
- **Classes & Objects**: Entities representing real-world banking domain objects (`User`, `Customer`, `Admin`, `Account`, `Transaction`).
- **Constructors & Chaining**: Parameterized constructors and `super()` chaining (`Customer.java` invokes `super(fullName, email, ...)`).
- **Encapsulation**: Strict `private` access modifiers on all entity fields with public getters, setters, and validation logic.
- **Inheritance**:
  - `User` (Base) $\rightarrow$ `Customer` (Child with phone, address, customerCode)
  - `User` (Base) $\rightarrow$ `Admin` (Child with department, accessLevel)
  - `Account` (Abstract Base) $\rightarrow$ `SavingsAccount` & `CurrentAccount`
- **Polymorphism**: Dynamic method dispatch through abstract methods:
  - `account.canWithdraw(amount)` behaves differently in `SavingsAccount` (minimum balance ₹1,000) versus `CurrentAccount` (minimum balance ₹500).
  - `account.calculateInterest(rate)` accrues 4% p.a. for Savings and 0% for Current.
  - *Source Files*: `src/com/banking/model/*.java`

### Unit 2: Abstraction, Interfaces & Packages
- **Abstract Classes**: `Account.java` defines common properties and forces polymorphic contracts via abstract methods.
- **Interfaces**: Clear abstraction between interface contracts and concrete implementations:
  - DAOs: `UserDAO`, `AccountDAO`, `TransactionDAO`, `AuditLogDAO`
  - Services: `AuthService`, `AccountService`, `TransactionService`, `AdminService`
- **Clean Layered Packages**: Structured into `model`, `exceptions`, `database`, `dao`, `services`, `threads`, `utilities`, and `gui`.
- *Source Files*: `src/com/banking/dao/*.java`, `src/com/banking/services/*.java`

### Unit 3: Exception Handling & Custom Exceptions
- **Custom Checked Exception Hierarchy**:
  - `BankingException` (Root checked exception with error codes)
  - `AuthenticationException` (Invalid credentials)
  - `InsufficientFundsException` (Balance breaches minimum balance rule)
  - `AccountFrozenException` (Operations on locked accounts blocked)
  - `AccountNotFoundException` (Invalid account number)
  - `InvalidPinException` (Incorrect 4-digit PIN)
  - `DuplicateUserException` (Email collision)
  - `ValidationException` (Malformed inputs)
- **Robust Exception Handling**: Try-with-resources blocks, nested exception wrapping, suppressed exceptions during database rollback.
- *Source Files*: `src/com/banking/exceptions/*.java`

### Unit 4: Collections, Generics & Java File I/O
- **Collections Framework**: Heavy use of `List<T>`, `ArrayList<T>`, `Optional<T>`, `BlockingQueue<T>`, `Map<K, V>`.
- **Generics**: Generic DAO methods, `Optional<User>`, and `TransactionWorker<T>` `SwingWorker` subclass.
- **File Handling & Java I/O**:
  - `StatementExporter.java`: Writes formatted bank statements to `.txt` files and `.csv` files using `BufferedWriter`, `FileWriter`, and `PrintWriter`.
  - `AsyncAuditLogger.java`: Appends real-time audit records to `logs/banking_audit.log`.
  - `ConfigLoader.java`: Reads properties via `FileInputStream` and `Properties.load()`.
- *Source Files*: `src/com/banking/utilities/StatementExporter.java`, `src/com/banking/threads/AsyncAuditLogger.java`

### Unit 5: Multithreading, Synchronization, JDBC & Swing GUI
- **Multithreading & Synchronization**:
  - `AsyncAuditLogger.java`: Producer-consumer thread-safe logging running on a daemon thread with `LinkedBlockingQueue` and `synchronized` file writes.
  - `BackgroundInterestWorker.java`: Scheduled background worker thread that processes interest calculations across accounts with thread-safe updates.
  - `TransactionWorker.java`: Asynchronous execution off the Event Dispatch Thread (EDT) using `SwingWorker`.
- **JDBC Database Connectivity**:
  - Singleton `DBConnection.java` connection manager.
  - Automatic table creation in `DatabaseInitializer.java`.
  - 100% PreparedStatements preventing SQL injection.
  - ACID Transactions: `connection.setAutoCommit(false)`, `commit()`, and `rollback()` in `AccountDAOImpl.transferFunds`.
- **Modern Java Swing GUI**:
  - `MainFrame.java` with responsive `CardLayout`.
  - Custom components: `CardPanel`, `ModernButton` (hover animations, rounded borders), `ModernTextField`, `ModernPasswordField`, `StatusBadge`, and `TransactionTableRenderer`.
  - Event Handling: `ActionListener`, `MouseAdapter`, `FocusAdapter`, `WindowAdapter`.
- *Source Files*: `src/com/banking/gui/**/*.java`, `src/com/banking/database/*.java`, `src/com/banking/threads/*.java`

---

## 🗄️ Database & ACID Transaction Consistency

### Zero-Setup Embedded SQLite (Default)
The project comes pre-configured with **SQLite JDBC** (`lib/sqlite-jdbc.jar`). When launched, it automatically creates `data/banking.db` and provisions all tables. No external database servers or installations are required!

### Optional MySQL Support
To switch to MySQL, edit `config.properties`:
```properties
db.type=mysql
db.mysql.host=localhost
db.mysql.port=3306
db.mysql.database=banking_system
db.mysql.username=root
db.mysql.password=your_password
```

### ACID Money Transfer Guarantee
In `AccountDAOImpl.transferFunds()`:
```java
conn.setAutoCommit(false); // Begin ACID Transaction
try {
    // 1. Verify and debit sender
    // 2. Verify and credit receiver
    // 3. Insert TRANSFER_OUT ledger record
    // 4. Insert TRANSFER_IN ledger record
    conn.commit(); // Atomic commit
} catch (SQLException e) {
    conn.rollback(); // Complete rollback on any failure
    throw e;
}
```

---

## 📁 Project Directory & Package Structure

```
Banking-System-Management/
├── build.bat                            # Windows automated compilation script
├── run-gui.bat                          # Windows one-click GUI launcher
├── run-cli.bat                          # Windows CLI console launcher
├── run-tests.bat                        # Automated test suite runner (26 tests)
├── config.properties                    # Active database & system configuration
├── config.properties.example            # Safe template configuration
├── .gitignore                           # Excludes binaries, db files, logs, and sensitive keys
├── README.md                            # Comprehensive documentation & evaluation guide
├── lib/                                 # JDBC Drivers
│   ├── sqlite-jdbc.jar                  # Embedded SQLite driver (default)
│   └── mysql-connector-j-8.3.0.jar      # MySQL connector driver (optional)
├── sql/
│   └── schema.sql                       # Database schema definition
├── data/                                # Auto-created directory for SQLite database (banking.db)
├── logs/                                # Auto-created directory for audit logs (banking_audit.log)
├── exports/                             # Auto-created directory for exported statements (.txt / .csv)
├── bin/                                 # Compiled bytecode (.class files)
└── src/
    └── com/
        └── banking/
            ├── Main.java                # Master Entry Point (dispatches to GUI or CLI)
            ├── BankingGUI.java          # Swing GUI Master Application Launcher
            ├── BankingCLI.java          # Terminal Console Interactive Launcher
            ├── model/                   # Domain Entities & Value Objects
            │   ├── User.java            # Base authenticated user entity
            │   ├── Customer.java        # Inherits User (phone, address, code)
            │   ├── Admin.java           # Inherits User (department, access)
            │   ├── Account.java         # Abstract base account (polymorphic)
            │   ├── SavingsAccount.java  # Inherits Account (min balance ₹1000, 4% interest)
            │   ├── CurrentAccount.java  # Inherits Account (min balance ₹500, 0% interest)
            │   ├── Transaction.java     # Immutable financial audit record
            │   ├── TransactionType.java # DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST
            │   ├── AccountType.java     # SAVINGS, CURRENT
            │   ├── AccountStatus.java   # ACTIVE, FROZEN, CLOSED
            │   ├── UserRole.java        # CUSTOMER, ADMIN
            │   └── SystemStats.java     # Macro metrics transfer object
            ├── exceptions/              # Custom Exception Hierarchy
            │   ├── BankingException.java
            │   ├── AuthenticationException.java
            │   ├── InsufficientFundsException.java
            │   ├── AccountFrozenException.java
            │   ├── AccountNotFoundException.java
            │   ├── InvalidPinException.java
            │   ├── DuplicateUserException.java
            │   └── ValidationException.java
            ├── database/                # Database Management & Schema Provisioning
            │   ├── DBConnection.java    # Thread-safe Singleton Connection Manager
            │   ├── DatabaseInitializer.java # Auto table creation & admin seeder
            │   └── DatabaseType.java    # SQLITE, MYSQL
            ├── dao/                     # Data Access Layer (Interfaces)
            │   ├── UserDAO.java
            │   ├── AccountDAO.java
            │   ├── TransactionDAO.java
            │   ├── AuditLogDAO.java
            │   └── impl/                # Concrete PreparedStatements Implementations
            │       ├── UserDAOImpl.java
            │       ├── AccountDAOImpl.java
            │       ├── TransactionDAOImpl.java
            │       └── AuditLogDAOImpl.java
            ├── services/                # Business Logic Layer (Interfaces)
            │   ├── AuthService.java
            │   ├── AccountService.java
            │   ├── TransactionService.java
            │   ├── AdminService.java
            │   └── impl/                # Concrete Business Service Implementations
            │       ├── AuthServiceImpl.java
            │       ├── AccountServiceImpl.java
            │       ├── TransactionServiceImpl.java
            │       └── AdminServiceImpl.java
            ├── threads/                 # Multithreading & Asynchronous Workers
            │   ├── AsyncAuditLogger.java # Background daemon audit logging queue
            │   ├── BackgroundInterestWorker.java # Periodic interest calculator
            │   └── TransactionWorker.java # SwingWorker for non-blocking UI
            ├── utilities/               # Transversal Helper Utilities
            │   ├── PasswordHasher.java  # SHA-256 password & PIN hashing
            │   ├── ValidationUtil.java  # Regex input sanitizers
            │   ├── NumberGenerator.java # Account number & TXN ref generator
            │   ├── ConfigLoader.java    # Safe properties loader
            │   ├── CurrencyFormatter.java # Uniform currency formatting (₹)
            │   └── StatementExporter.java # Java I/O statement text & CSV writer
            ├── gui/                     # Swing Graphical User Interface
            │   ├── MainFrame.java       # Master CardLayout Window Frame
            │   ├── theme/
            │   │   └── UITheme.java     # Navy, gold, white styling system
            │   ├── components/          # Reusable UI Widgets
            │   │   ├── CardPanel.java
            │   │   ├── ModernButton.java
            │   │   ├── ModernTextField.java
            │   │   ├── ModernPasswordField.java
            │   │   ├── StatusBadge.java
            │   │   └── TransactionTableRenderer.java
            │   ├── dialogs/             # Interactive Action Modals
            │   │   ├── DepositDialog.java
            │   │   ├── WithdrawDialog.java
            │   │   ├── TransferDialog.java
            │   │   ├── MiniStatementDialog.java
            │   │   └── ChangePinDialog.java
            │   └── panels/              # Full-Screen View Panels
            │       ├── LoginPanel.java
            │       ├── RegisterPanel.java
            │       ├── CustomerDashboardPanel.java
            │       └── AdminDashboardPanel.java
            └── test/                    # End-to-End Automated Test Suite
                └── BankingSystemTest.java
```

---

## 🚀 Compilation & Execution Instructions

### Prerequisites
- **JDK 17 or higher** (JDK 21 or JDK 27 supported).
- Ensure `java` and `javac` are available in your system `PATH`.

### Method A: One-Click Windows Batch Scripts (Recommended)

1. **Compile the Project**:
   Double-click `build.bat` or run:
   ```cmd
   build.bat
   ```
2. **Launch the Modern Swing GUI**:
   Double-click `run-gui.bat` or run:
   ```cmd
   run-gui.bat
   ```
3. **Launch the Command-Line Interface (CLI)**:
   Double-click `run-cli.bat` or run:
   ```cmd
   run-cli.bat
   ```
4. **Run the Automated Integration Test Suite (26 Tests)**:
   Double-click `run-tests.bat` or run:
   ```cmd
   run-tests.bat
   ```

### Method B: Manual CLI Commands

```cmd
# 1. Compile source files into bin/ directory
javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar;lib/mysql-connector-j-8.3.0.jar" -d bin @sources.txt

# 2. Run the Graphical User Interface
java -cp "bin;lib/sqlite-jdbc.jar;lib/mysql-connector-j-8.3.0.jar" com.banking.BankingGUI

# 3. Run the Command-Line Interface
java -cp "bin;lib/sqlite-jdbc.jar;lib/mysql-connector-j-8.3.0.jar" com.banking.BankingCLI

# 4. Run the Automated Test Suite
java -cp "bin;lib/sqlite-jdbc.jar;lib/mysql-connector-j-8.3.0.jar" com.banking.test.BankingSystemTest
```

---

## 🔑 Default Evaluation Credentials & Demo Guide

### 1. Administrator Portal Access
The application automatically seeds a secure Administrator account on the initial launch:
- **Email**: `admin@bank.com`
- **Password**: `Admin@123`
*(On the GUI login screen, click the **"Demo Admin"** button to auto-fill these credentials)*

### 2. Testing Customer Operations
1. Click **"Open an Account"** on the Login screen.
2. Fill in your details (e.g., Name: `Druthi Sharma`, Email: `druthi@example.com`, Password: `Password@123`, PIN: `1234`, Initial Deposit: `₹ 5,000.00`).
3. Click **"Open Account Now"**.
4. Sign in with your new email and password.
5. In the Customer Dashboard:
   - Click **"👁 Toggle Mask"** to reveal your actual balance (`₹ 5,000.00`).
   - Click **"💰 Deposit"** to credit ₹2,000.00.
   - Click **"💸 Withdraw"** to debit ₹1,000.00 (verified by your 4-digit PIN).
   - Click **"🔁 Transfer"** to send funds to another account with beneficiary verification.
   - Click **"📜 Mini Statement"** to view your recent operations.
   - Click **"📄 Export Ledger"** to save your passbook as a `.txt` or `.csv` file.

---

## 🎓 Student Viva & Technical Defense Guide

### Q1: How do you prevent race conditions and maintain ACID transaction consistency during fund transfers?
> **Answer:** We employ manual transaction demarcation in `AccountDAOImpl.transferFunds()`. We invoke `connection.setAutoCommit(false)` before executing the balance check, sender debit, receiver credit, and ledger entries. If all statements execute successfully, we invoke `connection.commit()`. If an exception occurs (such as insufficient funds, frozen status, or constraint failure), the `catch` block calls `connection.rollback()`, guaranteeing that no funds are lost or created.

### Q2: How does the application implement Polymorphism in account operations?
> **Answer:** We have an abstract base class `Account` with abstract methods `canWithdraw(amount)` and `calculateInterest(rate)`. `SavingsAccount` overrides `canWithdraw()` to enforce the strict ₹1,000.00 minimum balance rule and calculates 4.0% annual interest. `CurrentAccount` overrides `canWithdraw()` allowing balances down to ₹500.00 and calculates 0% interest. The service and transaction layers invoke `account.canWithdraw()` polymorphically without needing `instanceof` checks.

### Q3: How is password and PIN security guaranteed?
> **Answer:** Plaintext passwords and PINs are never stored. In `PasswordHasher.java`, we compute a 256-bit cryptographic digest using the **SHA-256** hashing algorithm. For verification, we re-hash user input and compare the byte digests using `MessageDigest.isEqual()` in constant time to prevent timing attacks.

### Q4: Where is Multithreading demonstrated in the system?
> **Answer:** Multithreading is implemented in three key areas:
> 1. `AsyncAuditLogger.java`: Uses a dedicated background daemon thread with a `LinkedBlockingQueue` to write audit entries to the log file and database asynchronously without blocking the UI.
> 2. `BackgroundInterestWorker.java`: A background runnable thread that periodically processes and credits interest accruals.
> 3. `TransactionWorker.java`: A `SwingWorker<T, Void>` subclass that offloads heavy database operations off the Swing Event Dispatch Thread (EDT) so the interface remains silky smooth.

---

### 👨‍💻 Project Branding
- **System**: Apex Digital Banking System (ADBS)
- **Engine**: Core Java Enterprise Banking Architecture
- **Version**: 2.0 (Enhanced from scratch)
