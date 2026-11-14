package com.banking.gui.dialogs;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernPasswordField;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.theme.UITheme;
import com.banking.model.Account;
import com.banking.services.AccountService;
import com.banking.services.TransactionService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for peer-to-peer inter-account fund transfers with beneficiary verification.
 */
public class TransferDialog extends JDialog {

    private final Account senderAccount;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Runnable onSuccessCallback;

    private ModernTextField receiverField;
    private JLabel beneficiaryNameLabel;
    private ModernTextField amountField;
    private ModernPasswordField pinField;
    private ModernTextField noteField;
    private Long verifiedReceiverAccount = null;

    public TransferDialog(Window parent, Account senderAccount, AccountService accountService,
                          TransactionService transactionService, Runnable onSuccessCallback) {
        super(parent, "Fund Transfer - Apex Digital Bank", ModalityType.APPLICATION_MODAL);
        this.senderAccount = senderAccount;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.onSuccessCallback = onSuccessCallback;

        initUI();
    }

    private void initUI() {
        setSize(520, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Transfer Funds");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("From Account #" + senderAccount.getAccountNumber() + " | Available: " +
                CurrencyFormatter.format(senderAccount.getBalance()));
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
        gbc.insets = new Insets(6, 6, 6, 6);

        // Destination Account & Verify Button
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel receiverLabel = new JLabel("Beneficiary Account #:");
        receiverLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(receiverLabel, gbc);

        JPanel receiverInputRow = new JPanel(new BorderLayout(8, 0));
        receiverInputRow.setOpaque(false);
        receiverField = new ModernTextField("10-digit Account Number", 12);
        ModernButton verifyBtn = new ModernButton("Verify", ModernButton.ButtonType.SECONDARY);
        verifyBtn.setMargin(new Insets(6, 12, 6, 12));
        verifyBtn.addActionListener(e -> verifyBeneficiary());
        receiverInputRow.add(receiverField, BorderLayout.CENTER);
        receiverInputRow.add(verifyBtn, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 0;
        formCard.add(receiverInputRow, gbc);

        // Beneficiary Name Display
        gbc.gridx = 1; gbc.gridy = 1;
        beneficiaryNameLabel = new JLabel("Status: Enter account and click 'Verify'");
        beneficiaryNameLabel.setFont(UITheme.FONT_SMALL);
        beneficiaryNameLabel.setForeground(UITheme.TEXT_MUTED);
        formCard.add(beneficiaryNameLabel, gbc);

        // Transfer Amount
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel amountLabel = new JLabel("Transfer Amount (" + CurrencyFormatter.getCurrencySymbol() + "):");
        amountLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(amountLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        amountField = new ModernTextField("e.g. 1500.00");
        formCard.add(amountField, gbc);

        // Security PIN
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel pinLabel = new JLabel("Your 4-Digit PIN:");
        pinLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(pinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        pinField = new ModernPasswordField("••••");
        formCard.add(pinField, gbc);

        // Note
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel noteLabel = new JLabel("Reference / Remarks:");
        noteLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(noteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        noteField = new ModernTextField("Rent, project fee, gift, etc.");
        formCard.add(noteField, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(UITheme.BG_MAIN);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonType.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton sendBtn = new ModernButton("Transfer Now", ModernButton.ButtonType.PRIMARY);
        sendBtn.addActionListener(e -> executeTransfer());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(sendBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void verifyBeneficiary() {
        String input = receiverField.getText().trim();
        if (input.isEmpty()) {
            beneficiaryNameLabel.setText("Please enter an account number.");
            beneficiaryNameLabel.setForeground(UITheme.DANGER_RED);
            return;
        }

        try {
            long accNum = Long.parseLong(input);
            if (accNum == senderAccount.getAccountNumber()) {
                beneficiaryNameLabel.setText("Cannot transfer to your own account.");
                beneficiaryNameLabel.setForeground(UITheme.DANGER_RED);
                verifiedReceiverAccount = null;
                return;
            }

            Account target = accountService.getAccountByNumber(accNum);
            verifiedReceiverAccount = target.getAccountNumber();
            beneficiaryNameLabel.setText("Beneficiary: " + target.getFullName() + " (" + target.getAccountType().getDisplayName() + ")");
            beneficiaryNameLabel.setForeground(UITheme.SUCCESS_GREEN);
        } catch (NumberFormatException e) {
            beneficiaryNameLabel.setText("Invalid account number format.");
            beneficiaryNameLabel.setForeground(UITheme.DANGER_RED);
            verifiedReceiverAccount = null;
        } catch (BankingException e) {
            beneficiaryNameLabel.setText(e.getMessage());
            beneficiaryNameLabel.setForeground(UITheme.DANGER_RED);
            verifiedReceiverAccount = null;
        }
    }

    private void executeTransfer() {
        if (verifiedReceiverAccount == null) {
            verifyBeneficiary();
            if (verifiedReceiverAccount == null) {
                JOptionPane.showMessageDialog(this, "Please verify the beneficiary account before proceeding.",
                        "Unverified Beneficiary", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a transfer amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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

        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm Fund Transfer:\n\n" +
                "• Recipient: " + beneficiaryNameLabel.getText() + "\n" +
                "• Account: #" + verifiedReceiverAccount + "\n" +
                "• Amount: " + CurrencyFormatter.format(amount) + "\n\n" +
                "Do you authorize this transaction?",
                "Confirm Transfer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String note = noteField.getText().trim();
        try {
            transactionService.transfer(senderAccount.getAccountNumber(), verifiedReceiverAccount, amount, pin, note);
            JOptionPane.showMessageDialog(this,
                    "Successfully transferred " + CurrencyFormatter.format(amount) + " to Account #" + verifiedReceiverAccount + "!",
                    "Transfer Completed", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transfer Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
}
