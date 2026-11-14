package com.banking.gui.dialogs;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernPasswordField;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.services.AccountService;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for securely updating an account's 4-digit transaction PIN.
 */
public class ChangePinDialog extends JDialog {

    private final Account account;
    private final AccountService accountService;

    private ModernPasswordField oldPinField;
    private ModernPasswordField newPinField;
    private ModernPasswordField confirmPinField;

    public ChangePinDialog(Window parent, Account account, AccountService accountService) {
        super(parent, "Update Security PIN - Apex Digital Bank", ModalityType.APPLICATION_MODAL);
        this.account = account;
        this.accountService = accountService;

        initUI();
    }

    private void initUI() {
        setSize(440, 380);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Change Security PIN");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Account #" + account.getAccountNumber());
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.ACCENT_GOLD);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.BG_MAIN);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        CardPanel formCard = new CardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Old PIN
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel oldPinLabel = new JLabel("Current 4-Digit PIN:");
        oldPinLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(oldPinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        oldPinField = new ModernPasswordField("••••");
        formCard.add(oldPinField, gbc);

        // New PIN
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel newPinLabel = new JLabel("New 4-Digit PIN:");
        newPinLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(newPinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        newPinField = new ModernPasswordField("••••");
        formCard.add(newPinField, gbc);

        // Confirm PIN
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel confirmPinLabel = new JLabel("Confirm New PIN:");
        confirmPinLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(confirmPinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        confirmPinField = new ModernPasswordField("••••");
        formCard.add(confirmPinField, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(UITheme.BG_MAIN);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonType.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton("Update PIN", ModernButton.ButtonType.PRIMARY);
        saveBtn.addActionListener(e -> executeChangePin());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void executeChangePin() {
        String oldPin = new String(oldPinField.getPassword()).trim();
        String newPin = new String(newPinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();

        if (oldPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all PIN fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPin.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "New PIN must be exactly 4 numeric digits.", "Invalid PIN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(this, "The new PIN and confirmation PIN do not match.", "Mismatch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            accountService.updatePin(account.getAccountNumber(), oldPin, newPin);
            JOptionPane.showMessageDialog(this, "Security PIN updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (BankingException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "PIN Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
