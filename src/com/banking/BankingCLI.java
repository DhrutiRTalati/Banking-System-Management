package com.banking;

import com.banking.database.DatabaseInitializer;
import com.banking.exceptions.BankingException;
import com.banking.model.Account;
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
import com.banking.utilities.CurrencyFormatter;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * Modern Command-Line Interface (CLI) for Apex Digital Banking System.
 * Shares the exact same DAO, database, and service layers with the Swing GUI.
 */
public class BankingCLI {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AdminService adminService;
    private final Scanner scanner;

    public BankingCLI() {
        this.authService = new AuthServiceImpl();
        this.accountService = new AccountServiceImpl();
        this.transactionService = new TransactionServiceImpl();
        this.adminService = new AdminServiceImpl();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Database initialization warning: " + e.getMessage());
        }

        BankingCLI cli = new BankingCLI();
        cli.run();
    }

    public void run() {
        printBanner();

        boolean running = true;
        while (running) {
            System.out.println("\n========================================================");
            System.out.println("             APEX DIGITAL BANK - MAIN MENU              ");
            System.out.println("========================================================");
            System.out.println("1. Customer Sign In");
            System.out.println("2. Open New Customer Account");
            System.out.println("3. Administrator Portal Sign In");
            System.out.println("4. Exit Application");
            System.out.print("Select an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleCustomerLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    handleAdminLogin();
                    break;
                case "4":
                    System.out.println("\nThank you for choosing Apex Digital Bank. Goodbye!");
                    AsyncAuditLogger.getInstance().shutdown();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option between 1 and 4.");
            }
        }
    }

    private void printBanner() {
        System.out.println("========================================================");
        System.out.println("        🏛️  APEX DIGITAL BANKING MANAGEMENT SYSTEM      ");
        System.out.println("        Enterprise Core Banking & ACID Ledger Engine     ");
        System.out.println("========================================================");
    }

    private void handleCustomerLogin() {
        System.out.println("\n--- CUSTOMER SIGN IN ---");
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine().trim();

        try {
            User user = authService.login(email, pass);
            if (user.getRole() == UserRole.ADMIN) {
                System.out.println("Notice: This is an administrative account. Redirecting to Admin Console.");
                adminDashboardMenu(user);
            } else {
                Customer customer = (user instanceof Customer) ? (Customer) user :
                        new Customer(user.getId(), user.getFullName(), user.getEmail(), user.getPasswordHash(), "", "", "", user.getCreatedAt());
                customerDashboardMenu(customer);
            }
        } catch (BankingException e) {
            System.out.println("❌ Login Failed: " + e.getMessage());
        }
    }

    private void handleRegistration() {
        System.out.println("\n--- OPEN NEW BANK ACCOUNT ---");
        System.out.print("Full Legal Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password (min 6 chars): ");
        String password = scanner.nextLine().trim();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Residential Address: ");
        String address = scanner.nextLine().trim();

        System.out.println("Account Type:");
        System.out.println("  1. Savings Account (Min Balance ₹1,000 | 4.0% p.a. interest)");
        System.out.println("  2. Current Account (Min Balance ₹500 | Business transactions)");
        System.out.print("Choose Type (1 or 2): ");
        String typeChoice = scanner.nextLine().trim();
        AccountType type = "2".equals(typeChoice) ? AccountType.CURRENT : AccountType.SAVINGS;

        double minBal = (type == AccountType.CURRENT) ? 500.0 : 1000.0;
        System.out.printf("Initial Deposit (Minimum ₹%.2f): ", minBal);
        double deposit;
        try {
            deposit = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid numerical amount.");
            return;
        }

        System.out.print("Set 4-Digit Transaction PIN: ");
        String pin = scanner.nextLine().trim();
        System.out.print("Confirm 4-Digit Transaction PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!pin.equals(confirmPin)) {
            System.out.println("❌ PIN confirmation does not match.");
            return;
        }

        try {
            Customer c = authService.registerCustomer(name, email, password, phone, address, type, deposit, pin);
            System.out.println("\n🎉 Account Created Successfully!");
            System.out.println("Customer Code : " + c.getCustomerCode());
            System.out.println("Account Holder: " + c.getFullName());
            System.out.println("You can now sign in using your email and password.");
        } catch (BankingException e) {
            System.out.println("❌ Registration Failed: " + e.getMessage());
        }
    }

    private void handleAdminLogin() {
        System.out.println("\n--- ADMINISTRATOR PORTAL SIGN IN ---");
        System.out.println("(Default evaluation credentials: admin@bank.com / Admin@123)");
        System.out.print("Admin Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Admin Password: ");
        String pass = scanner.nextLine().trim();

        try {
            User user = authService.login(email, pass);
            if (user.getRole() != UserRole.ADMIN) {
                System.out.println("❌ Access Denied: User does not possess administrative privileges.");
                return;
            }
            adminDashboardMenu(user);
        } catch (BankingException e) {
            System.out.println("❌ Authentication Failed: " + e.getMessage());
        }
    }

    private void customerDashboardMenu(Customer customer) {
        boolean inPortal = true;
        while (inPortal) {
            try {
                Account account = accountService.getAccountByEmail(customer.getEmail());

                System.out.println("\n========================================================");
                System.out.printf("  CUSTOMER DASHBOARD - %s (%s)\n", customer.getFullName(), customer.getCustomerCode());
                System.out.printf("  Account #%-12d | %s | %s\n",
                        account.getAccountNumber(), account.getAccountType().getDisplayName(), account.getStatus().getLabel());
                System.out.println("========================================================");
                System.out.println("1. Balance Enquiry (Masked / Unmasked)");
                System.out.println("2. Deposit Money");
                System.out.println("3. Withdraw Money");
                System.out.println("4. Transfer Funds (P2P)");
                System.out.println("5. View Mini Statement (Last 5)");
                System.out.println("6. View Full Passbook Ledger");
                System.out.println("7. Export Statement to File (.txt / .csv)");
                System.out.println("8. Change Security PIN");
                System.out.println("9. Sign Out");
                System.out.print("Choose an operation (1-9): ");

                String op = scanner.nextLine().trim();
                switch (op) {
                    case "1":
                        showBalanceEnquiry(account);
                        break;
                    case "2":
                        executeDeposit(account);
                        break;
                    case "3":
                        executeWithdrawal(account);
                        break;
                    case "4":
                        executeTransfer(account);
                        break;
                    case "5":
                        showMiniStatement(account);
                        break;
                    case "6":
                        showFullPassbook(account);
                        break;
                    case "7":
                        executeStatementExport(account);
                        break;
                    case "8":
                        executeChangePin(account);
                        break;
                    case "9":
                        System.out.println("Signed out of customer portal.");
                        inPortal = false;
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
            } catch (BankingException e) {
                System.out.println("❌ Error loading portal: " + e.getMessage());
                break;
            }
        }
    }

    private void showBalanceEnquiry(Account account) {
        System.out.println("\n--- BALANCE ENQUIRY ---");
        System.out.println("Masked Balance: " + account.getMaskedBalance());
        System.out.print("Reveal actual balance? (y/n): ");
        String ans = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(ans)) {
            System.out.println("Available Balance: " + CurrencyFormatter.format(account.getBalance()));
            System.out.println("Account Policy   : " + account.getAccountPolicyDescription());
        }
    }

    private void executeDeposit(Account account) {
        System.out.println("\n--- CASH / ONLINE DEPOSIT ---");
        System.out.printf("Current Balance: %s\n", CurrencyFormatter.format(account.getBalance()));
        System.out.print("Enter Amount to Deposit: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Remarks / Description: ");
            String note = scanner.nextLine().trim();

            Transaction tx = transactionService.deposit(account.getAccountNumber(), amount, note);
            System.out.printf("✅ Successfully deposited %s. New Balance: %s [Ref: %s]\n",
                    CurrencyFormatter.format(amount), CurrencyFormatter.format(tx.getBalanceAfter()), tx.getTransactionRef());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format.");
        } catch (BankingException e) {
            System.out.println("❌ Deposit Error: " + e.getMessage());
        }
    }

    private void executeWithdrawal(Account account) {
        System.out.println("\n--- WITHDRAWAL ---");
        System.out.printf("Available Balance: %s\n", CurrencyFormatter.format(account.getBalance()));
        System.out.println("Policy: " + account.getAccountPolicyDescription());
        System.out.print("Enter Amount to Withdraw: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter 4-Digit Security PIN: ");
            String pin = scanner.nextLine().trim();
            System.out.print("Remarks / Note: ");
            String note = scanner.nextLine().trim();

            Transaction tx = transactionService.withdraw(account.getAccountNumber(), amount, pin, note);
            System.out.printf("✅ Successfully withdrew %s. Remaining Balance: %s [Ref: %s]\n",
                    CurrencyFormatter.format(amount), CurrencyFormatter.format(tx.getBalanceAfter()), tx.getTransactionRef());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format.");
        } catch (BankingException e) {
            System.out.println("❌ Withdrawal Error: " + e.getMessage());
        }
    }

    private void executeTransfer(Account account) {
        System.out.println("\n--- FUND TRANSFER (P2P) ---");
        System.out.print("Enter Beneficiary Account #: ");
        try {
            long targetAcc = Long.parseLong(scanner.nextLine().trim());
            Account target = accountService.getAccountByNumber(targetAcc);
            System.out.printf("Beneficiary Found: %s (%s)\n", target.getFullName(), target.getAccountType().getDisplayName());

            System.out.print("Enter Transfer Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Your 4-Digit Security PIN: ");
            String pin = scanner.nextLine().trim();

            System.out.print("Reference Note: ");
            String note = scanner.nextLine().trim();

            System.out.printf("Authorize transfer of %s to %s (#%d)? (y/n): ",
                    CurrencyFormatter.format(amount), target.getFullName(), targetAcc);
            if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) {
                System.out.println("Transfer cancelled.");
                return;
            }

            transactionService.transfer(account.getAccountNumber(), targetAcc, amount, pin, note);
            System.out.println("✅ Transfer Completed Successfully!");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid numerical input.");
        } catch (BankingException e) {
            System.out.println("❌ Transfer Error: " + e.getMessage());
        }
    }

    private void showMiniStatement(Account account) {
        System.out.println("\n--- MINI STATEMENT (LAST 5 TRANSACTIONS) ---");
        try {
            List<Transaction> list = transactionService.getMiniStatement(account.getAccountNumber());
            printTransactionTable(list);
        } catch (BankingException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void showFullPassbook(Account account) {
        System.out.println("\n--- FULL PASSBOOK LEDGER ---");
        try {
            List<Transaction> list = transactionService.getTransactionHistory(account.getAccountNumber(), 50);
            printTransactionTable(list);
        } catch (BankingException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void printTransactionTable(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No recorded transactions found.");
            return;
        }
        System.out.printf("%-19s | %-16s | %-14s | %-12s | %-12s | %s\n",
                "Date & Time", "Reference", "Type", "Amount", "Balance After", "Description");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (Transaction tx : list) {
            String sign = tx.getType().isCredit() ? "+ " : "- ";
            System.out.printf("%-19s | %-16s | %-14s | %-12s | %-12s | %s\n",
                    tx.getFormattedTimestamp(),
                    tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                    tx.getType().getDescription(),
                    sign + CurrencyFormatter.format(tx.getAmount()),
                    CurrencyFormatter.format(tx.getBalanceAfter()),
                    tx.getDescription() != null ? tx.getDescription() : "-");
        }
    }

    private void executeStatementExport(Account account) {
        System.out.println("\n--- EXPORT STATEMENT TO DISK ---");
        System.out.println("1. Formatted Text File (.txt)");
        System.out.println("2. Spreadsheet CSV (.csv)");
        System.out.print("Select format (1 or 2): ");
        String fmt = scanner.nextLine().trim();
        boolean isCsv = "2".equals(fmt);

        String filename = "exports/Statement_" + account.getAccountNumber() + (isCsv ? ".csv" : ".txt");
        File target = new File(filename);

        try {
            transactionService.exportStatement(account.getAccountNumber(), target, isCsv);
            System.out.println("✅ Statement successfully saved to: " + target.getAbsolutePath());
        } catch (BankingException e) {
            System.out.println("❌ Export Error: " + e.getMessage());
        }
    }

    private void executeChangePin(Account account) {
        System.out.println("\n--- UPDATE SECURITY PIN ---");
        System.out.print("Enter Current 4-Digit PIN: ");
        String oldPin = scanner.nextLine().trim();
        System.out.print("Enter New 4-Digit PIN: ");
        String newPin = scanner.nextLine().trim();
        System.out.print("Confirm New 4-Digit PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!newPin.equals(confirmPin)) {
            System.out.println("❌ New PINs do not match.");
            return;
        }

        try {
            accountService.updatePin(account.getAccountNumber(), oldPin, newPin);
            System.out.println("✅ Security PIN updated successfully!");
        } catch (BankingException e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void adminDashboardMenu(User admin) {
        boolean inAdmin = true;
        while (inAdmin) {
            try {
                SystemStats stats = adminService.getSystemStatistics();

                System.out.println("\n========================================================");
                System.out.println("             ADMINISTRATOR CONTROL CONSOLE              ");
                System.out.println("========================================================");
                System.out.printf("Customers: %d | Accounts: %d (Active: %d, Frozen: %d)\n",
                        stats.getTotalCustomers(), stats.getTotalAccounts(), stats.getActiveAccounts(), stats.getFrozenAccounts());
                System.out.printf("Total Liquidity: %s | Total Recorded Transactions: %d\n",
                        CurrencyFormatter.format(stats.getTotalLiquidity()), stats.getTotalTransactions());
                System.out.println("--------------------------------------------------------");
                System.out.println("1. View All Registered Customers");
                System.out.println("2. Search Customers");
                System.out.println("3. View All Bank Accounts");
                System.out.println("4. Freeze Customer Account");
                System.out.println("5. Unfreeze Customer Account");
                System.out.println("6. View System-Wide Transaction Audit Ledger");
                System.out.println("7. Trigger Savings Interest Calculation (4.0% p.a.)");
                System.out.println("8. Sign Out");
                System.out.print("Select action (1-8): ");

                String act = scanner.nextLine().trim();
                switch (act) {
                    case "1":
                        showAllCustomers();
                        break;
                    case "2":
                        searchCustomers();
                        break;
                    case "3":
                        showAllAccounts();
                        break;
                    case "4":
                        freezeAccountAction();
                        break;
                    case "5":
                        unfreezeAccountAction();
                        break;
                    case "6":
                        showSystemAudit();
                        break;
                    case "7":
                        int count = adminService.triggerInterestAccrual();
                        System.out.println("✅ Credited monthly interest to " + count + " active savings accounts.");
                        break;
                    case "8":
                        System.out.println("Signed out of administrative console.");
                        inAdmin = false;
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
            } catch (BankingException e) {
                System.out.println("❌ Administrative Error: " + e.getMessage());
                break;
            }
        }
    }

    private void showAllCustomers() {
        System.out.println("\n--- REGISTERED CUSTOMER DIRECTORY ---");
        try {
            List<Customer> list = adminService.getAllCustomers();
            System.out.printf("%-6s | %-12s | %-22s | %-26s | %-14s\n", "ID", "Code", "Full Name", "Email", "Phone");
            System.out.println("--------------------------------------------------------------------------------------------");
            for (Customer c : list) {
                System.out.printf("%-6d | %-12s | %-22s | %-26s | %-14s\n",
                        c.getId(), c.getCustomerCode() != null ? c.getCustomerCode() : "-",
                        c.getFullName(), c.getEmail(), c.getPhoneNumber() != null ? c.getPhoneNumber() : "-");
            }
        } catch (BankingException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void searchCustomers() {
        System.out.print("\nEnter keyword to search (name, email, or customer code): ");
        String q = scanner.nextLine().trim();
        try {
            List<Customer> list = adminService.searchCustomers(q);
            System.out.printf("Found %d matching customer(s):\n", list.size());
            for (Customer c : list) {
                System.out.printf(" • [%s] %s | %s | %s\n",
                        c.getCustomerCode(), c.getFullName(), c.getEmail(), c.getPhoneNumber());
            }
        } catch (BankingException e) {
            System.out.println("❌ Search Error: " + e.getMessage());
        }
    }

    private void showAllAccounts() {
        System.out.println("\n--- BANK ACCOUNT LEDGER ---");
        try {
            List<Account> list = adminService.getAllAccounts();
            System.out.printf("%-12s | %-22s | %-12s | %-14s | %-8s\n", "Account #", "Account Holder", "Type", "Balance", "Status");
            System.out.println("---------------------------------------------------------------------------------");
            for (Account acc : list) {
                System.out.printf("%-12d | %-22s | %-12s | %-14s | %-8s\n",
                        acc.getAccountNumber(), acc.getFullName(), acc.getAccountType().name(),
                        CurrencyFormatter.format(acc.getBalance()), acc.getStatus().name());
            }
        } catch (BankingException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void freezeAccountAction() {
        System.out.print("\nEnter Account # to freeze: ");
        try {
            long accNum = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Provide reason/justification: ");
            String reason = scanner.nextLine().trim();

            adminService.freezeAccount(accNum, reason);
            System.out.println("✅ Account #" + accNum + " has been FROZEN. All operations are now locked.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid account number.");
        } catch (BankingException e) {
            System.out.println("❌ Freeze Error: " + e.getMessage());
        }
    }

    private void unfreezeAccountAction() {
        System.out.print("\nEnter Account # to unfreeze: ");
        try {
            long accNum = Long.parseLong(scanner.nextLine().trim());
            adminService.unfreezeAccount(accNum);
            System.out.println("✅ Account #" + accNum + " restored to ACTIVE status.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid account number.");
        } catch (BankingException e) {
            System.out.println("❌ Unfreeze Error: " + e.getMessage());
        }
    }

    private void showSystemAudit() {
        System.out.println("\n--- SYSTEM-WIDE AUDIT LEDGER (LAST 50 TRANSACTIONS) ---");
        try {
            List<Transaction> list = adminService.getSystemTransactions(50);
            printTransactionTable(list);
        } catch (BankingException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
