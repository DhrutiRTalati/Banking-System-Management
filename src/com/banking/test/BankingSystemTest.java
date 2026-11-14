package com.banking.test;

import com.banking.database.DatabaseInitializer;
import com.banking.exceptions.AccountFrozenException;
import com.banking.exceptions.AuthenticationException;
import com.banking.exceptions.InsufficientFundsException;
import com.banking.exceptions.InvalidPinException;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.model.Customer;
import com.banking.model.SystemStats;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.model.UserRole;
import com.banking.services.AccountService;
import com.banking.services.AdminService;
import com.banking.services.AuthService;
import com.banking.services.TransactionService;
import com.banking.services.impl.AccountServiceImpl;
import com.banking.services.impl.AdminServiceImpl;
import com.banking.services.impl.AuthServiceImpl;
import com.banking.services.impl.TransactionServiceImpl;
import com.banking.threads.AsyncAuditLogger;
import com.banking.utilities.PasswordHasher;

import java.io.File;
import java.util.List;

/**
 * Automated End-to-End System Integration Test Suite.
 * Verifies all major banking operations, ACID transactions, polymorphic account rules,
 * exception scenarios, and multithreading.
 */
public class BankingSystemTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   RUNNING APEX BANKING SYSTEM INTEGRATION TEST SUITE   ");
        System.out.println("========================================================");

        try {
            // 1. Initialize DB schema
            DatabaseInitializer.initialize();
            assertCondition(true, "Database schema auto-creation and admin seeding");

            AuthService authService = new AuthServiceImpl();
            AccountService accountService = new AccountServiceImpl();
            TransactionService transactionService = new TransactionServiceImpl();
            AdminService adminService = new AdminServiceImpl();

            // 2. Cryptographic Security & Password Hashing
            String plaintext = "SecretPassword123";
            String hash = PasswordHasher.hash(plaintext);
            assertCondition(hash.length() == 64, "SHA-256 produces 64-char hexadecimal digest");
            assertCondition(PasswordHasher.verify(plaintext, hash), "PasswordHasher.verify validates exact digest");
            assertCondition(!PasswordHasher.verify("WrongPassword", hash), "PasswordHasher.verify rejects mismatch");

            // 3. Admin Authentication
            User adminUser = authService.login("admin@bank.com", "Admin@123");
            assertCondition(adminUser != null && adminUser.getRole() == UserRole.ADMIN, "Default Administrator login successful");

            // 4. Customer Registration (Savings Account)
            long stamp = System.currentTimeMillis() % 100000;
            String aliceEmail = "alice" + stamp + "@test.com";
            Customer alice = authService.registerCustomer(
                    "Alice Walker", aliceEmail, "AlicePass123", "+919876543210",
                    "123 Park Avenue", AccountType.SAVINGS, 5000.0, "1234"
            );
            assertCondition(alice != null && alice.getId() > 0, "Alice customer registered with ID: " + alice.getId());

            Account aliceAcc = accountService.getAccountByEmail(aliceEmail);
            assertCondition(aliceAcc != null && aliceAcc.getBalance() == 5000.0, "Alice savings account opened with balance 5000.00");
            assertCondition(aliceAcc.getAccountType() == AccountType.SAVINGS, "Alice account recognized as SAVINGS");

            // 5. Customer Registration (Current Account)
            String bobEmail = "bob" + stamp + "@test.com";
            Customer bob = authService.registerCustomer(
                    "Bob Builder", bobEmail, "BobPass123", "+919123456780",
                    "456 Market Road", AccountType.CURRENT, 3000.0, "4321"
            );
            Account bobAcc = accountService.getAccountByEmail(bobEmail);
            assertCondition(bobAcc != null && bobAcc.getBalance() == 3000.0, "Bob current account opened with balance 3000.00");

            // 6. Deposit Operation
            Transaction depTx = transactionService.deposit(aliceAcc.getAccountNumber(), 2000.0, "Salary Deposit");
            assertCondition(depTx != null && depTx.getBalanceAfter() == 7000.0, "Alice deposited 2000.00 -> New balance: 7000.00");

            // 7. Withdrawal with PIN Verification
            Transaction wTx = transactionService.withdraw(aliceAcc.getAccountNumber(), 1500.0, "1234", "Grocery Shopping");
            assertCondition(wTx != null && wTx.getBalanceAfter() == 5500.0, "Alice withdrew 1500.00 -> New balance: 5500.00");

            // 8. Withdrawal with Invalid PIN (Exception Handling)
            boolean pinCaught = false;
            try {
                transactionService.withdraw(aliceAcc.getAccountNumber(), 500.0, "9999", "Failed attempt");
            } catch (InvalidPinException e) {
                pinCaught = true;
            }
            assertCondition(pinCaught, "Withdrawal with invalid PIN correctly throws InvalidPinException");

            // 9. Savings Account Minimum Balance Enforcement (Polymorphic Rule)
            boolean minBalCaught = false;
            try {
                // Balance is 5500. Savings min balance is 1000. Attempting to withdraw 4800 leaves 700 < 1000.
                transactionService.withdraw(aliceAcc.getAccountNumber(), 4800.0, "1234", "Excessive withdrawal");
            } catch (InsufficientFundsException e) {
                minBalCaught = true;
            }
            assertCondition(minBalCaught, "Savings Account enforces ₹1,000.00 minimum balance rule via InsufficientFundsException");

            // 10. Inter-Account Fund Transfer (P2P ACID Transaction)
            boolean transferOk = transactionService.transfer(
                    aliceAcc.getAccountNumber(), bobAcc.getAccountNumber(), 1500.0, "1234", "Project Payment"
            );
            assertCondition(transferOk, "Transferred 1500.00 from Alice to Bob atomically");

            Account aliceAfterTransfer = accountService.getAccountByNumber(aliceAcc.getAccountNumber());
            Account bobAfterTransfer = accountService.getAccountByNumber(bobAcc.getAccountNumber());
            assertCondition(aliceAfterTransfer.getBalance() == 4000.0, "Alice balance debited to 4000.00");
            assertCondition(bobAfterTransfer.getBalance() == 4500.0, "Bob balance credited to 4500.00");

            // 11. Transaction History & Mini Statement
            List<Transaction> aliceMini = transactionService.getMiniStatement(aliceAcc.getAccountNumber());
            assertCondition(aliceMini.size() <= 5 && !aliceMini.isEmpty(), "Mini statement returns recent transactions (count: " + aliceMini.size() + ")");

            // 12. File Statement Export (Java I/O)
            File exportTxt = new File("exports/Test_Statement_" + aliceAcc.getAccountNumber() + ".txt");
            boolean txtExported = transactionService.exportStatement(aliceAcc.getAccountNumber(), exportTxt, false);
            assertCondition(txtExported && exportTxt.exists() && exportTxt.length() > 0, "Statement exported to formatted TXT file");

            File exportCsv = new File("exports/Test_Statement_" + aliceAcc.getAccountNumber() + ".csv");
            boolean csvExported = transactionService.exportStatement(aliceAcc.getAccountNumber(), exportCsv, true);
            assertCondition(csvExported && exportCsv.exists() && exportCsv.length() > 0, "Statement exported to CSV spreadsheet file");

            // 13. Admin Account Freeze / Fraud Control
            adminService.freezeAccount(bobAcc.getAccountNumber(), "Suspicious international transfer activity");
            Account bobFrozen = accountService.getAccountByNumber(bobAcc.getAccountNumber());
            assertCondition(bobFrozen.getStatus() == AccountStatus.FROZEN, "Bob account status successfully set to FROZEN");

            // Attempt withdrawal on frozen account
            boolean frozenCaught = false;
            try {
                transactionService.withdraw(bobAcc.getAccountNumber(), 100.0, "4321", "Test frozen");
            } catch (AccountFrozenException e) {
                frozenCaught = true;
            }
            assertCondition(frozenCaught, "Operation on frozen account correctly blocked via AccountFrozenException");

            // Admin Unfreeze
            adminService.unfreezeAccount(bobAcc.getAccountNumber());
            Account bobUnfrozen = accountService.getAccountByNumber(bobAcc.getAccountNumber());
            assertCondition(bobUnfrozen.getStatus() == AccountStatus.ACTIVE, "Bob account restored to ACTIVE status");

            // 14. Background Interest Calculation Worker
            int credited = adminService.triggerInterestAccrual();
            assertCondition(credited >= 1, "Interest Accrual worker processed active savings accounts (credited: " + credited + ")");

            // 15. Admin System Macro Metrics
            SystemStats stats = adminService.getSystemStatistics();
            assertCondition(stats.getTotalCustomers() >= 2, "Admin stats report >= 2 customers");
            assertCondition(stats.getTotalLiquidity() > 0, "Admin stats report positive total liquidity: ₹" + stats.getTotalLiquidity());
            assertCondition(stats.getTotalTransactions() >= 5, "Admin stats report >= 5 recorded transactions");

            // Shutdown async audit worker
            AsyncAuditLogger.getInstance().shutdown();

            System.out.println("========================================================");
            System.out.printf("   ALL TESTS COMPLETED: %d / %d PASSED (100%% SUCCESS)    \n", testsPassed, testsRun);
            System.out.println("========================================================");

        } catch (Throwable t) {
            System.err.println("❌ TEST RUNNER FAILED WITH EXCEPTION:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String description) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] Test %02d: %s\n", testsRun, description);
        } else {
            System.err.printf("  [FAIL] Test %02d: %s\n", testsRun, description);
            throw new AssertionError("Test failed: " + description);
        }
    }
}
