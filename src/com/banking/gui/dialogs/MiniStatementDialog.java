package com.banking.gui.dialogs;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.TransactionTableRenderer;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.services.TransactionService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Modal dialog presenting a mini-statement (last 5-10 operations) with one-click disk export.
 */
public class MiniStatementDialog extends JDialog {

    private final Account account;
    private final TransactionService transactionService;
    private JTable table;
    private DefaultTableModel tableModel;

    public MiniStatementDialog(Window parent, Account account, TransactionService transactionService) {
        super(parent, "Mini Statement - Apex Digital Bank", ModalityType.APPLICATION_MODAL);
        this.account = account;
        this.transactionService = transactionService;

        initUI();
        loadTransactions();
    }

    private void initUI() {
        setSize(780, 480);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Mini Statement");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Account #" + account.getAccountNumber() + " | " + account.getFullName() +
                " | Available: " + CurrencyFormatter.format(account.getBalance()));
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.ACCENT_GOLD);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.BG_MAIN);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        CardPanel card = new CardPanel(new BorderLayout());

        String[] columns = {"Date & Time", "Reference", "Type", "Amount", "Balance After", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_SUBHEADER);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        table.setDefaultRenderer(Object.class, new TransactionTableRenderer(3, 2));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        card.add(scrollPane, BorderLayout.CENTER);

        centerContainer.add(card, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footerPanel.setBackground(UITheme.BG_MAIN);

        ModernButton exportTxtBtn = new ModernButton("Export to TXT", ModernButton.ButtonType.SECONDARY);
        exportTxtBtn.addActionListener(e -> exportToFile(false));

        ModernButton exportCsvBtn = new ModernButton("Export to CSV", ModernButton.ButtonType.GOLD);
        exportCsvBtn.addActionListener(e -> exportToFile(true));

        ModernButton closeBtn = new ModernButton("Close", ModernButton.ButtonType.PRIMARY);
        closeBtn.addActionListener(e -> dispose());

        footerPanel.add(exportTxtBtn);
        footerPanel.add(exportCsvBtn);
        footerPanel.add(closeBtn);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        try {
            List<Transaction> list = transactionService.getMiniStatement(account.getAccountNumber());
            if (list.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "-", "NO TRANSACTIONS", "-", "-", "No history recorded yet."});
                return;
            }

            for (Transaction tx : list) {
                String typeSign = tx.getType().isCredit() ? "+ " : "- ";
                tableModel.addRow(new Object[]{
                        tx.getFormattedTimestamp(),
                        tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                        tx.getType().getDescription(),
                        typeSign + CurrencyFormatter.format(tx.getAmount()),
                        CurrencyFormatter.format(tx.getBalanceAfter()),
                        tx.getDescription() != null ? tx.getDescription() : "-"
                });
            }
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, "Could not load transactions: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToFile(boolean isCsv) {
        JFileChooser fileChooser = new JFileChooser(new File("exports"));
        String defaultName = "Statement_" + account.getAccountNumber() + (isCsv ? ".csv" : ".txt");
        fileChooser.setSelectedFile(new File("exports/" + defaultName));

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                transactionService.exportStatement(account.getAccountNumber(), selected, isCsv);
                JOptionPane.showMessageDialog(this,
                        "Statement exported successfully to:\n" + selected.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (BankingException e) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
