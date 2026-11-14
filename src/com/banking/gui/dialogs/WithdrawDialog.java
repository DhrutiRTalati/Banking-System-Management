package com.banking.gui.dialogs;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernPasswordField;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.services.TransactionService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for withdrawing funds from an account with PIN verification.
 */
public class WithdrawDialog extends JDialog {

    private final Account account;
    private final TransactionService transactionService;
    private final Runnable onSuccessCallback;

    private ModernTextField amountField;
    private ModernPasswordField pinField;
    private ModernTextField noteField;

    public WithdrawDialog(Window parent, Account account, TransactionService transactionService,
                          Runnable onSuccessCallback) {
        super(parent, "Withdraw Funds - Apex Digital Bank", ModalityType.APPLICATION_MODAL);
        this.account = account;
        this.transactionService = transactionService;
        this.onSuccessCallback = onSuccessCallback;

        initUI();
    }

    private void initUI() {
        setSize(480, 470);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Withdraw Money");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Account #" + account.getAccountNumber() + " | " + account.getAccountType().getDisplayName());
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.ACCENT_GOLD);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.BG_MAIN);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        CardPanel formCard = new CardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Balance & Policy Info
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel curBalLabel = new JLabel("Available Balance: " + CurrencyFormatter.format(account.getBalance()));
        curBalLabel.setFont(UITheme.FONT_BODY_BOLD);
        curBalLabel.setForeground(UITheme.TEXT_SECONDARY);
        formCard.add(curBalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JLabel policyLabel = new JLabel(account.getAccountPolicyDescription());
        policyLabel.setFont(UITheme.FONT_SMALL);
        policyLabel.setForeground(UITheme.TEXT_MUTED);
        formCard.add(policyLabel, gbc);

        // Amount Field
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel amountLabel = new JLabel("Amount (" + CurrencyFormatter.getCurrencySymbol() + "):");
        amountLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(amountLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        amountField = new ModernTextField("e.g. 2000.00");
        formCard.add(amountField, gbc);

        // Security PIN Field
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel pinLabel = new JLabel("4-Digit PIN:");
        pinLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(pinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        pinField = new ModernPasswordField("••••");
        formCard.add(pinField, gbc);

        // Description / Note
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel noteLabel = new JLabel("Remarks / Note:");
        noteLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(noteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        noteField = new ModernTextField("ATM withdrawal, utility bills, etc.");
        formCard.add(noteField, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(UITheme.BG_MAIN);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonType.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton confirmBtn = new ModernButton("Confirm Withdrawal", ModernButton.ButtonType.DANGER);
        confirmBtn.addActionListener(e -> executeWithdrawal());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void executeWithdrawal() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a withdrawal amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid numeric number.", "Invalid Format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pin = new String(pinField.getPassword()).trim();
        if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "Please enter your 4-digit numeric PIN.", "Invalid PIN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String note = noteField.getText().trim();
        try {
            transactionService.withdraw(account.getAccountNumber(), amount, pin, note);
            JOptionPane.showMessageDialog(this,
                    "Withdrawal of " + CurrencyFormatter.format(amount) + " completed successfully!",
                    "Withdrawal Successful", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Withdrawal Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
}
