package com.banking.gui.panels;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.components.StatusBadge;
import com.banking.gui.components.TransactionTableRenderer;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.Customer;
import com.banking.model.SystemStats;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.services.AdminService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Enterprise Administrator Control Console.
 * Features: Macro System KPI statistics, Customer search and directory,
 * Fraud prevention account freeze/unfreeze actions, and a full system-wide audit ledger.
 */
public class AdminDashboardPanel extends JPanel {

    private final AdminService adminService;
    private final Runnable onLogout;
    private User currentAdmin;

    // KPI Metric Labels
    private JLabel totalCustomersLabel;
    private JLabel totalAccountsLabel;
    private JLabel activeAccountsLabel;
    private JLabel frozenAccountsLabel;
    private JLabel totalLiquidityLabel;
    private JLabel totalTransactionsLabel;

    // Customers Tab
    private ModernTextField customerSearchField;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;

    // Accounts Tab
    private JTable accountsTable;
    private DefaultTableModel accountsTableModel;
    private List<Account> cachedAccounts;

    // System Audit Tab
    private JTable auditTable;
    private DefaultTableModel auditTableModel;

    public AdminDashboardPanel(AdminService adminService, Runnable onLogout) {
        this.adminService = adminService;
        this.onLogout = onLogout;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);

        // 1. Top Navbar
        add(createTopNavBar(), BorderLayout.NORTH);

        // 2. Center Content
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UITheme.BG_MAIN);
        mainContent.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // KPI Stats Row
        mainContent.add(createKpiStatsRow());
        mainContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // Action Toolbar
        mainContent.add(createActionToolbar());
        mainContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // Tabbed View
        mainContent.add(createTabbedManagementView());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTopNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UITheme.PRIMARY_NAVY);
        navBar.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel brandGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandGroup.setOpaque(false);

        JLabel logo = new JLabel("🛡️");
        logo.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 24));

        JLabel brandName = new JLabel("APEX BANKING - ADMINISTRATIVE CONSOLE");
        brandName.setFont(UITheme.FONT_HEADER);
        brandName.setForeground(Color.WHITE);

        brandGroup.add(logo);
        brandGroup.add(brandName);
        navBar.add(brandGroup, BorderLayout.WEST);

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightGroup.setOpaque(false);

        JLabel adminLabel = new JLabel("Logged in as System Admin");
        adminLabel.setFont(UITheme.FONT_BODY_BOLD);
        adminLabel.setForeground(UITheme.ACCENT_GOLD);

        ModernButton refreshBtn = new ModernButton("Refresh All", ModernButton.ButtonType.OUTLINE);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setMargin(new Insets(4, 10, 4, 10));
        refreshBtn.addActionListener(e -> refreshAllData());

        ModernButton logoutBtn = new ModernButton("Sign Out", ModernButton.ButtonType.DANGER);
        logoutBtn.setMargin(new Insets(4, 12, 4, 12));
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        rightGroup.add(adminLabel);
        rightGroup.add(refreshBtn);
        rightGroup.add(logoutBtn);
        navBar.add(rightGroup, BorderLayout.EAST);

        return navBar;
    }

    private JPanel createKpiStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 6, 12, 0));
        row.setOpaque(false);

        totalCustomersLabel = new JLabel("0", SwingConstants.CENTER);
        totalAccountsLabel = new JLabel("0", SwingConstants.CENTER);
        activeAccountsLabel = new JLabel("0", SwingConstants.CENTER);
        frozenAccountsLabel = new JLabel("0", SwingConstants.CENTER);
        totalLiquidityLabel = new JLabel("₹ 0.00", SwingConstants.CENTER);
        totalTransactionsLabel = new JLabel("0", SwingConstants.CENTER);

        row.add(createKpiCard("TOTAL CUSTOMERS", totalCustomersLabel, UITheme.PRIMARY_NAVY));
        row.add(createKpiCard("TOTAL ACCOUNTS", totalAccountsLabel, UITheme.SECONDARY_NAVY));
        row.add(createKpiCard("ACTIVE ACCOUNTS", activeAccountsLabel, UITheme.SUCCESS_GREEN));
        row.add(createKpiCard("FROZEN ACCOUNTS", frozenAccountsLabel, UITheme.DANGER_RED));
        row.add(createKpiCard("TOTAL LIQUIDITY", totalLiquidityLabel, UITheme.ACCENT_GOLD.darker()));
        row.add(createKpiCard("TRANSACTIONS", totalTransactionsLabel, UITheme.PRIMARY_NAVY));

        return row;
    }

    private CardPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel.setFont(UITheme.FONT_HEADER);
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionToolbar() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel tipLabel = new JLabel("Automated Background Operations & Fraud Controls");
        tipLabel.setFont(UITheme.FONT_BODY_BOLD);
        tipLabel.setForeground(UITheme.TEXT_PRIMARY);
        card.add(tipLabel, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        ModernButton interestBtn = new ModernButton("📈 Process Savings Interest (4.0% p.a.)", ModernButton.ButtonType.GOLD);
        interestBtn.addActionListener(e -> processInterest());

        rightButtons.add(interestBtn);
        card.add(rightButtons, BorderLayout.EAST);

        return card;
    }

    private JPanel createTabbedManagementView() {
        CardPanel card = new CardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEADER);

        tabs.addTab("👤 Customer Directory", createCustomerTab());
        tabs.addTab("🔒 Account & Freeze Management", createAccountTab());
        tabs.addTab("📜 System-Wide Audit Ledger", createAuditTab());

        card.add(tabs, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        customerSearchField = new ModernTextField("Search by Name, Email, or Customer Code...", 20);
        ModernButton searchBtn = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        searchBtn.addActionListener(e -> executeCustomerSearch());

        ModernButton clearBtn = new ModernButton("Reset", ModernButton.ButtonType.OUTLINE);
        clearBtn.addActionListener(e -> {
            customerSearchField.setText("");
            loadAllCustomers();
        });

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(searchBtn);
        btnGroup.add(clearBtn);

        searchBar.add(customerSearchField, BorderLayout.CENTER);
        searchBar.add(btnGroup, BorderLayout.EAST);
        panel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Customer Code", "Full Name", "Email Address", "Phone Number", "Registered Date"};
        customerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(32);
        customerTable.setFont(UITheme.FONT_BODY);
        customerTable.getTableHeader().setFont(UITheme.FONT_SUBHEADER);
        customerTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scroll = new JScrollPane(customerTable);
        scroll.setPreferredSize(new Dimension(800, 360));
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAccountTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        // Top Toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topBar.setOpaque(false);

        ModernButton freezeBtn = new ModernButton("❄️ Freeze Selected Account", ModernButton.ButtonType.DANGER);
        freezeBtn.addActionListener(e -> handleFreezeSelected());

        ModernButton unfreezeBtn = new ModernButton("🔓 Unfreeze Account", ModernButton.ButtonType.SUCCESS);
        unfreezeBtn.addActionListener(e -> handleUnfreezeSelected());

        ModernButton viewDetailsBtn = new ModernButton("ℹ️ Account Details", ModernButton.ButtonType.SECONDARY);
        viewDetailsBtn.addActionListener(e -> handleViewAccountDetails());

        topBar.add(viewDetailsBtn);
        topBar.add(freezeBtn);
        topBar.add(unfreezeBtn);
        panel.add(topBar, BorderLayout.NORTH);

        // Accounts Table
        String[] columns = {"Account #", "Account Holder", "Email", "Type", "Balance", "Status", "Created At"};
        accountsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountsTable = new JTable(accountsTableModel);
        accountsTable.setRowHeight(32);
        accountsTable.setFont(UITheme.FONT_BODY);
        accountsTable.getTableHeader().setFont(UITheme.FONT_SUBHEADER);
        accountsTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scroll = new JScrollPane(accountsTable);
        scroll.setPreferredSize(new Dimension(800, 360));
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuditTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        String[] columns = {"Timestamp", "Reference", "Account #", "Type", "Amount", "Balance After", "Description"};
        auditTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        auditTable = new JTable(auditTableModel);
        auditTable.setRowHeight(32);
        auditTable.setFont(UITheme.FONT_BODY);
        auditTable.getTableHeader().setFont(UITheme.FONT_SUBHEADER);
        auditTable.getTableHeader().setBackground(new Color(241, 245, 249));
        auditTable.setDefaultRenderer(Object.class, new TransactionTableRenderer(4, 3));

        JScrollPane scroll = new JScrollPane(auditTable);
        scroll.setPreferredSize(new Dimension(800, 380));
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void setAdmin(User admin) {
        this.currentAdmin = admin;
        refreshAllData();
    }

    public void refreshAllData() {
        loadKpiStatistics();
        loadAllCustomers();
        loadAllAccounts();
        loadAuditTransactions();
    }

    private void loadKpiStatistics() {
        try {
            SystemStats stats = adminService.getSystemStatistics();
            totalCustomersLabel.setText(String.valueOf(stats.getTotalCustomers()));
            totalAccountsLabel.setText(String.valueOf(stats.getTotalAccounts()));
            activeAccountsLabel.setText(String.valueOf(stats.getActiveAccounts()));
            frozenAccountsLabel.setText(String.valueOf(stats.getFrozenAccounts()));
            totalLiquidityLabel.setText(CurrencyFormatter.format(stats.getTotalLiquidity()));
            totalTransactionsLabel.setText(String.valueOf(stats.getTotalTransactions()));
        } catch (BankingException e) {
            System.err.println("Could not load stats: " + e.getMessage());
        }
    }

    private void loadAllCustomers() {
        customerTableModel.setRowCount(0);
        try {
            List<Customer> list = adminService.getAllCustomers();
            for (Customer c : list) {
                customerTableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCustomerCode() != null ? c.getCustomerCode() : "-",
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhoneNumber() != null ? c.getPhoneNumber() : "-",
                        c.getCreatedAt() != null ? c.getCreatedAt().toString() : "-"
                });
            }
        } catch (BankingException e) {
            System.err.println("Could not load customers: " + e.getMessage());
        }
    }

    private void executeCustomerSearch() {
        String q = customerSearchField.getText().trim();
        if (q.isEmpty()) {
            loadAllCustomers();
            return;
        }

        customerTableModel.setRowCount(0);
        try {
            List<Customer> list = adminService.searchCustomers(q);
            for (Customer c : list) {
                customerTableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCustomerCode() != null ? c.getCustomerCode() : "-",
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhoneNumber() != null ? c.getPhoneNumber() : "-",
                        c.getCreatedAt() != null ? c.getCreatedAt().toString() : "-"
                });
            }
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllAccounts() {
        accountsTableModel.setRowCount(0);
        try {
            cachedAccounts = adminService.getAllAccounts();
            for (Account acc : cachedAccounts) {
                accountsTableModel.addRow(new Object[]{
                        acc.getAccountNumber(),
                        acc.getFullName(),
                        acc.getEmail(),
                        acc.getAccountType().getDisplayName(),
                        CurrencyFormatter.format(acc.getBalance()),
                        acc.getStatus().getLabel(),
                        acc.getCreatedAt() != null ? acc.getCreatedAt().toString() : "-"
                });
            }
        } catch (BankingException e) {
            System.err.println("Could not load accounts: " + e.getMessage());
        }
    }

    private void loadAuditTransactions() {
        auditTableModel.setRowCount(0);
        try {
            List<Transaction> list = adminService.getSystemTransactions(200);
            for (Transaction tx : list) {
                String typeSign = tx.getType().isCredit() ? "+ " : "- ";
                auditTableModel.addRow(new Object[]{
                        tx.getFormattedTimestamp(),
                        tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                        tx.getAccountNumber(),
                        tx.getType().getDescription(),
                        typeSign + CurrencyFormatter.format(tx.getAmount()),
                        CurrencyFormatter.format(tx.getBalanceAfter()),
                        tx.getDescription() != null ? tx.getDescription() : "-"
                });
            }
        } catch (BankingException e) {
            System.err.println("Could not load audit transactions: " + e.getMessage());
        }
    }

    private void handleFreezeSelected() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account row from the table.",
                    "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accountNumber = (Long) accountsTableModel.getValueAt(selectedRow, 0);
        String holderName = (String) accountsTableModel.getValueAt(selectedRow, 1);
        String currentStatus = (String) accountsTableModel.getValueAt(selectedRow, 5);

        if ("FROZEN".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Account #" + accountNumber + " is already FROZEN.",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Provide a justification / reason for freezing Account #" + accountNumber + " (" + holderName + "):",
                "Freeze Account Reason", JOptionPane.WARNING_MESSAGE);

        if (reason == null) return; // User cancelled

        try {
            adminService.freezeAccount(accountNumber, reason);
            JOptionPane.showMessageDialog(this, "Account #" + accountNumber + " has been successfully FROZEN.",
                    "Account Locked", JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Failed to freeze account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUnfreezeSelected() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account row from the table.",
                    "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accountNumber = (Long) accountsTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) accountsTableModel.getValueAt(selectedRow, 5);

        if ("ACTIVE".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Account #" + accountNumber + " is already ACTIVE.",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Restore Account #" + accountNumber + " to ACTIVE status?\nAll debit, credit, and transfer restrictions will be lifted.",
                "Confirm Unfreeze", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            adminService.unfreezeAccount(accountNumber);
            JOptionPane.showMessageDialog(this, "Account #" + accountNumber + " has been restored to ACTIVE status.",
                    "Account Restored", JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Failed to unfreeze account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewAccountDetails() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account row to view details.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accountNumber = (Long) accountsTableModel.getValueAt(selectedRow, 0);
        try {
            Account acc = adminService.getAccountDetails(accountNumber);
            String info = String.format(
                    "ACCOUNT DETAILS\n" +
                    "────────────────────────────────────────\n" +
                    "Account Number : %d\n" +
                    "Account Holder : %s\n" +
                    "Email Address  : %s\n" +
                    "Account Type   : %s\n" +
                    "Current Balance: %s\n" +
                    "Account Status : %s\n" +
                    "Policy Rules   : %s\n" +
                    "Opened Date    : %s\n",
                    acc.getAccountNumber(),
                    acc.getFullName(),
                    acc.getEmail(),
                    acc.getAccountType().getDisplayName(),
                    CurrencyFormatter.format(acc.getBalance()),
                    acc.getStatus().getLabel(),
                    acc.getAccountPolicyDescription(),
                    acc.getCreatedAt() != null ? acc.getCreatedAt().toString() : "-"
            );
            JOptionPane.showMessageDialog(this, info, "Account #" + accountNumber + " Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Failed to load account details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processInterest() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Execute Interest Calculation Worker now?\n" +
                "This will calculate accrued interest (4.0% p.a.) on all ACTIVE Savings Accounts and credit them immediately.",
                "Simulate Interest", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                int count = adminService.triggerInterestAccrual();
                JOptionPane.showMessageDialog(this,
                        "Interest successfully processed and credited to " + count + " active savings accounts!",
                        "Interest Processed", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            } catch (BankingException e) {
                JOptionPane.showMessageDialog(this, "Failed to run interest calculation: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
