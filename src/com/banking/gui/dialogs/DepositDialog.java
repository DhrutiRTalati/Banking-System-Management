package com.banking.gui.dialogs;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.services.TransactionService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for depositing funds into an account.
 */
public class DepositDialog extends JDialog {

    private final Account account;
    private final TransactionService transactionService;
    private final Runnable onSuccessCallback;

    private ModernTextField amountField;
    private ModernTextField noteField;
    private JLabel previewBalanceLabel;

    public DepositDialog(Window parent, Account account, TransactionService transactionService,
                         Runnable onSuccessCallback) {
        super(parent, "Deposit Funds - Apex Digital Bank", ModalityType.APPLICATION_MODAL);
        this.account = account;
        this.transactionService = transactionService;
        this.onSuccessCallback = onSuccessCallback;

        initUI();
    }

    private void initUI() {
        setSize(460, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Deposit Money");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Account #" + account.getAccountNumber() + " (" + account.getFullName() + ")");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.ACCENT_GOLD);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content Form Card
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.BG_MAIN);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        CardPanel formCard = new CardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Available Balance Info
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel curBalLabel = new JLabel("Current Balance: " + CurrencyFormatter.format(account.getBalance()));
        curBalLabel.setFont(UITheme.FONT_BODY_BOLD);
        curBalLabel.setForeground(UITheme.TEXT_SECONDARY);
        formCard.add(curBalLabel, gbc);

        // Amount Field
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel amountLabel = new JLabel("Deposit Amount (" + CurrencyFormatter.getCurrencySymbol() + "):");
        amountLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(amountLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        amountField = new ModernTextField("e.g. 5000.00");
        formCard.add(amountField, gbc);

        // Description / Note
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel noteLabel = new JLabel("Remarks / Note:");
        noteLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(noteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        noteField = new ModernTextField("Cash deposit, salary, etc.");
        formCard.add(noteField, gbc);

        // Projected New Balance
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        previewBalanceLabel = new JLabel("Projected Balance: " + CurrencyFormatter.format(account.getBalance()));
        previewBalanceLabel.setFont(UITheme.FONT_BODY);
        previewBalanceLabel.setForeground(UITheme.SUCCESS_GREEN);
        formCard.add(previewBalanceLabel, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Button Action Bar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(UITheme.BG_MAIN);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonType.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton confirmBtn = new ModernButton("Confirm Deposit", ModernButton.ButtonType.SUCCESS);
        confirmBtn.addActionListener(e -> executeDeposit());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void executeDeposit() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid deposit amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid numerical value.", "Invalid Format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String note = noteField.getText().trim();
        try {
            transactionService.deposit(account.getAccountNumber(), amount, note);
            JOptionPane.showMessageDialog(this,
                    "Deposit of " + CurrencyFormatter.format(amount) + " completed successfully!",
                    "Deposit Successful", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
