package com.banking.gui.panels;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.StatusBadge;
import com.banking.gui.components.TransactionTableRenderer;
import com.banking.gui.dialogs.ChangePinDialog;
import com.banking.gui.dialogs.DepositDialog;
import com.banking.gui.dialogs.MiniStatementDialog;
import com.banking.gui.dialogs.TransferDialog;
import com.banking.gui.dialogs.WithdrawDialog;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.model.Customer;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.services.AccountService;
import com.banking.services.TransactionService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;

/**
 * Customer Banking Portal Dashboard.
 * Provides balance enquiry, privacy masking toggle, financial actions,
 * interactive passbook ledger, and file exports.
 */
public class CustomerDashboardPanel extends JPanel {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Runnable onLogout;

    private Customer currentCustomer;
    private Account currentAccount;

    // UI elements to update on refresh
    private JLabel welcomeLabel;
    private JLabel customerCodeLabel;
    private JLabel accountNumberLabel;
    private JLabel balanceLabel;
    private StatusBadge statusBadge;
    private JLabel accountTypeBadge;
    private JLabel policyLabel;
    private boolean isBalanceMasked = true;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;

    public CustomerDashboardPanel(AccountService accountService, TransactionService transactionService,
                                  Runnable onLogout) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.onLogout = onLogout;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);

        // 1. Navigation Top Bar
        add(createTopNavBar(), BorderLayout.NORTH);

        // 2. Main Content Scrollable Area
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UITheme.BG_MAIN);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Account Overview Hero Card
        mainContent.add(createAccountHeroCard());
        mainContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // Quick Actions Grid
        mainContent.add(createActionButtonsCard());
        mainContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // Passbook / Transaction History Card
        mainContent.add(createTransactionLedgerCard());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTopNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UITheme.PRIMARY_NAVY);
        navBar.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Left Branding
        JPanel brandGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandGroup.setOpaque(false);

        JLabel logo = new JLabel("🏦");
        logo.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 26));

        JLabel brandName = new JLabel("APEX DIGITAL BANK");
        brandName.setFont(UITheme.FONT_HEADER);
        brandName.setForeground(Color.WHITE);

        brandGroup.add(logo);
        brandGroup.add(brandName);
        navBar.add(brandGroup, BorderLayout.WEST);

        // Right Customer Info & Logout
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userGroup.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, Customer");
        welcomeLabel.setFont(UITheme.FONT_BODY_BOLD);
        welcomeLabel.setForeground(Color.WHITE);

        customerCodeLabel = new JLabel("CUST-00000");
        customerCodeLabel.setFont(UITheme.FONT_SMALL);
        customerCodeLabel.setForeground(UITheme.ACCENT_GOLD);

        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.ButtonType.OUTLINE);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setMargin(new Insets(4, 10, 4, 10));
        refreshBtn.addActionListener(e -> refreshData());

        ModernButton logoutBtn = new ModernButton("Sign Out", ModernButton.ButtonType.GOLD);
        logoutBtn.setMargin(new Insets(4, 12, 4, 12));
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        userGroup.add(welcomeLabel);
        userGroup.add(customerCodeLabel);
        userGroup.add(refreshBtn);
        userGroup.add(logoutBtn);
        navBar.add(userGroup, BorderLayout.EAST);

        return navBar;
    }

    private JPanel createAccountHeroCard() {
        CardPanel card = new CardPanel(16);
        card.setLayout(new BorderLayout());
        card.setCustomBackground(UITheme.SECONDARY_NAVY);
        card.setBorderColor(null);
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // Top Row: Account Number & Badges
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel accNumGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accNumGroup.setOpaque(false);

        JLabel accPrefix = new JLabel("ACCOUNT NUMBER:");
        accPrefix.setFont(UITheme.FONT_SMALL);
        accPrefix.setForeground(UITheme.TEXT_MUTED);

        accountNumberLabel = new JLabel("0000000000");
        accountNumberLabel.setFont(UITheme.FONT_HEADER);
        accountNumberLabel.setForeground(Color.WHITE);

        JButton copyBtn = new JButton("📋 Copy");
        copyBtn.setFont(UITheme.FONT_SMALL);
        copyBtn.setForeground(UITheme.ACCENT_GOLD);
        copyBtn.setContentAreaFilled(false);
        copyBtn.setBorderPainted(false);
        copyBtn.setFocusPainted(false);
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyBtn.addActionListener(e -> {
            if (currentAccount != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(String.valueOf(currentAccount.getAccountNumber())), null
                );
                JOptionPane.showMessageDialog(this, "Account number copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        accNumGroup.add(accPrefix);
        accNumGroup.add(accountNumberLabel);
        accNumGroup.add(copyBtn);
        topRow.add(accNumGroup, BorderLayout.WEST);

        // Right Badges
        JPanel badgeGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badgeGroup.setOpaque(false);

        accountTypeBadge = new JLabel("SAVINGS ACCOUNT");
        accountTypeBadge.setFont(UITheme.FONT_SMALL);
        accountTypeBadge.setForeground(UITheme.ACCENT_GOLD);

        statusBadge = new StatusBadge(com.banking.model.AccountStatus.ACTIVE);

        badgeGroup.add(accountTypeBadge);
        badgeGroup.add(statusBadge);
        topRow.add(badgeGroup, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Center Row: Masked Balance & Eye Toggle
        JPanel balanceRow = new JPanel(new BorderLayout(14, 0));
        balanceRow.setOpaque(false);
        balanceRow.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));

        JPanel balLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        balLeft.setOpaque(false);

        JLabel balTitle = new JLabel("AVAILABLE BALANCE:");
        balTitle.setFont(UITheme.FONT_SMALL);
        balTitle.setForeground(UITheme.TEXT_MUTED);

        balanceLabel = new JLabel("₹ ••••••••");
        balanceLabel.setFont(UITheme.FONT_BALANCE);
        balanceLabel.setForeground(Color.WHITE);

        JButton toggleEyeBtn = new JButton("👁 Toggle Mask");
        toggleEyeBtn.setFont(UITheme.FONT_BODY_BOLD);
        toggleEyeBtn.setForeground(UITheme.ACCENT_GOLD);
        toggleEyeBtn.setContentAreaFilled(false);
        toggleEyeBtn.setBorderPainted(false);
        toggleEyeBtn.setFocusPainted(false);
        toggleEyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleEyeBtn.addActionListener(e -> toggleBalanceVisibility());

        balLeft.add(balTitle);
        balLeft.add(balanceLabel);
        balLeft.add(toggleEyeBtn);
        balanceRow.add(balLeft, BorderLayout.CENTER);

        card.add(balanceRow, BorderLayout.CENTER);

        // Bottom Row: Policy details
        policyLabel = new JLabel("Savings Account | Minimum Balance ₹1,000.00 required");
        policyLabel.setFont(UITheme.FONT_SMALL);
        policyLabel.setForeground(new Color(203, 213, 225));
        card.add(policyLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionButtonsCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new GridLayout(1, 6, 12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        ModernButton depositBtn = new ModernButton("💰 Deposit", ModernButton.ButtonType.SUCCESS);
        depositBtn.addActionListener(e -> openDepositDialog());

        ModernButton withdrawBtn = new ModernButton("💸 Withdraw", ModernButton.ButtonType.DANGER);
        withdrawBtn.addActionListener(e -> openWithdrawDialog());

        ModernButton transferBtn = new ModernButton("🔁 Transfer", ModernButton.ButtonType.PRIMARY);
        transferBtn.addActionListener(e -> openTransferDialog());

        ModernButton miniStmtBtn = new ModernButton("📜 Mini Statement", ModernButton.ButtonType.SECONDARY);
        miniStmtBtn.addActionListener(e -> openMiniStatementDialog());

        ModernButton exportBtn = new ModernButton("📄 Export Ledger", ModernButton.ButtonType.GOLD);
        exportBtn.addActionListener(e -> openExportDialog());

        ModernButton pinBtn = new ModernButton("🔑 Security PIN", ModernButton.ButtonType.OUTLINE);
        pinBtn.addActionListener(e -> openChangePinDialog());

        card.add(depositBtn);
        card.add(withdrawBtn);
        card.add(transferBtn);
        card.add(miniStmtBtn);
        card.add(exportBtn);
        card.add(pinBtn);

        return card;
    }

    private JPanel createTransactionLedgerCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Header with Filter Combo
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Account Passbook & Transactions");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterGroup.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter By Type:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);

        filterComboBox = new JComboBox<>(new String[]{"All Transactions", "Deposits Only", "Withdrawals Only", "Transfers Only"});
        filterComboBox.setFont(UITheme.FONT_BODY);
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.addActionListener(e -> applyFilter());

        filterGroup.add(filterLbl);
        filterGroup.add(filterComboBox);
        header.add(filterGroup, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"Date & Time", "Transaction Ref", "Type", "Amount", "Balance After", "Ref Account", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(34);
        transactionTable.setFont(UITheme.FONT_BODY);
        transactionTable.getTableHeader().setFont(UITheme.FONT_SUBHEADER);
        transactionTable.getTableHeader().setBackground(new Color(241, 245, 249));
        transactionTable.getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        transactionTable.setDefaultRenderer(Object.class, new TransactionTableRenderer(3, 2));

        JScrollPane tableScroll = new JScrollPane(transactionTable);
        tableScroll.setPreferredSize(new Dimension(800, 320));
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        card.add(tableScroll, BorderLayout.CENTER);

        return card;
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        refreshData();
    }

    public void refreshData() {
        if (currentCustomer == null) return;

        try {
            welcomeLabel.setText("Welcome, " + currentCustomer.getFullName());
            customerCodeLabel.setText(currentCustomer.getCustomerCode() != null ? currentCustomer.getCustomerCode() : "");

            currentAccount = accountService.getAccountByEmail(currentCustomer.getEmail());
            accountNumberLabel.setText(String.valueOf(currentAccount.getAccountNumber()));
            accountTypeBadge.setText(currentAccount.getAccountType().getDisplayName().toUpperCase());
            statusBadge.setStatus(currentAccount.getStatus());
            policyLabel.setText(currentAccount.getAccountPolicyDescription());

            updateBalanceDisplay();
            loadTransactions(null);

        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Failed to refresh account data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleBalanceVisibility() {
        isBalanceMasked = !isBalanceMasked;
        updateBalanceDisplay();
    }

    private void updateBalanceDisplay() {
        if (currentAccount == null) return;
        if (isBalanceMasked) {
            balanceLabel.setText(currentAccount.getMaskedBalance());
        } else {
            balanceLabel.setText(CurrencyFormatter.format(currentAccount.getBalance()));
        }
    }

    private void applyFilter() {
        int index = filterComboBox.getSelectedIndex();
        TransactionType filterType = null;
        if (index == 1) filterType = TransactionType.DEPOSIT;
        else if (index == 2) filterType = TransactionType.WITHDRAWAL;
        else if (index == 3) filterType = TransactionType.TRANSFER_OUT; // or TRANSFER_IN
        loadTransactions(filterType);
    }

    private void loadTransactions(TransactionType type) {
        if (currentAccount == null) return;
        tableModel.setRowCount(0);

        try {
            List<Transaction> list;
            int filterIndex = filterComboBox.getSelectedIndex();
            if (filterIndex == 0) {
                list = transactionService.getTransactionHistory(currentAccount.getAccountNumber(), 100);
            } else if (filterIndex == 1) {
                list = transactionService.getFilteredTransactions(currentAccount.getAccountNumber(), TransactionType.DEPOSIT, 100);
            } else if (filterIndex == 2) {
                list = transactionService.getFilteredTransactions(currentAccount.getAccountNumber(), TransactionType.WITHDRAWAL, 100);
            } else {
                list = transactionService.getTransactionHistory(currentAccount.getAccountNumber(), 100);
                list.removeIf(t -> t.getType() != TransactionType.TRANSFER_IN && t.getType() != TransactionType.TRANSFER_OUT);
            }

            for (Transaction tx : list) {
                String typeSign = tx.getType().isCredit() ? "+ " : "- ";
                tableModel.addRow(new Object[]{
                        tx.getFormattedTimestamp(),
                        tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                        tx.getType().getDescription(),
                        typeSign + CurrencyFormatter.format(tx.getAmount()),
                        CurrencyFormatter.format(tx.getBalanceAfter()),
                        tx.getRefAccount() != null ? ("#" + tx.getRefAccount()) : "-",
                        tx.getDescription() != null ? tx.getDescription() : "-"
                });
            }
        } catch (BankingException e) {
            System.err.println("Could not load transaction ledger: " + e.getMessage());
        }
    }

    private void openDepositDialog() {
        if (currentAccount == null) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        DepositDialog dialog = new DepositDialog(window, currentAccount, transactionService, this::refreshData);
        dialog.setVisible(true);
    }

    private void openWithdrawDialog() {
        if (currentAccount == null) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        WithdrawDialog dialog = new WithdrawDialog(window, currentAccount, transactionService, this::refreshData);
        dialog.setVisible(true);
    }

    private void openTransferDialog() {
        if (currentAccount == null) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        TransferDialog dialog = new TransferDialog(window, currentAccount, accountService, transactionService, this::refreshData);
        dialog.setVisible(true);
    }

    private void openMiniStatementDialog() {
        if (currentAccount == null) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        MiniStatementDialog dialog = new MiniStatementDialog(window, currentAccount, transactionService);
        dialog.setVisible(true);
    }

    private void openChangePinDialog() {
        if (currentAccount == null) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        ChangePinDialog dialog = new ChangePinDialog(window, currentAccount, accountService);
        dialog.setVisible(true);
    }

    private void openExportDialog() {
        if (currentAccount == null) return;
        String[] options = {"Text File (.txt)", "Spreadsheet CSV (.csv)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select the export format for Account #" + currentAccount.getAccountNumber() + ":",
                "Export Statement", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0 || choice == 1) {
            boolean isCsv = (choice == 1);
            JFileChooser fileChooser = new JFileChooser(new File("exports"));
            String defaultName = "Statement_" + currentAccount.getAccountNumber() + (isCsv ? ".csv" : ".txt");
            fileChooser.setSelectedFile(new File("exports/" + defaultName));

            int saveOption = fileChooser.showSaveDialog(this);
            if (saveOption == JFileChooser.APPROVE_OPTION) {
                File target = fileChooser.getSelectedFile();
                try {
                    transactionService.exportStatement(currentAccount.getAccountNumber(), target, isCsv);
                    JOptionPane.showMessageDialog(this,
                            "Statement successfully saved to:\n" + target.getAbsolutePath(),
                            "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (BankingException ex) {
                    JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
