package com.banking.gui.panels;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernPasswordField;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.theme.UITheme;
import com.banking.model.AccountType;
import com.banking.model.Customer;
import com.banking.model.SavingsAccount;
import com.banking.services.AuthService;
import com.banking.utilities.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern customer onboarding and bank account creation panel.
 */
public class RegisterPanel extends JPanel {

    private final AuthService authService;
    private final Consumer<Customer> onRegisterSuccess;
    private final Runnable onNavigateLogin;

    private ModernTextField fullNameField;
    private ModernTextField emailField;
    private ModernPasswordField passwordField;
    private ModernTextField phoneField;
    private ModernTextField addressField;
    private JComboBox<AccountType> accountTypeCombo;
    private JLabel policyInfoLabel;
    private ModernTextField depositField;
    private ModernPasswordField pinField;
    private ModernPasswordField confirmPinField;
    private JLabel errorLabel;

    public RegisterPanel(AuthService authService, Consumer<Customer> onRegisterSuccess, Runnable onNavigateLogin) {
        this.authService = authService;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateLogin = onNavigateLogin;

        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_MAIN);

        // Center Registration Card
        CardPanel card = new CardPanel(16);
        card.setPreferredSize(new Dimension(560, 680));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        // 1. Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Create Your Bank Account", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Instant online registration with immediate account opening", SwingConstants.CENTER);
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        card.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid inside a scroll pane for clean responsiveness
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        formGrid.add(createFieldLabel("Full Legal Name"), gbc);
        gbc.gridx = 1;
        fullNameField = new ModernTextField("e.g. Johnathan Doe", 15);
        formGrid.add(fullNameField, gbc);

        // Email Address
        gbc.gridx = 0; gbc.gridy = 1;
        formGrid.add(createFieldLabel("Email Address"), gbc);
        gbc.gridx = 1;
        emailField = new ModernTextField("name@example.com", 15);
        formGrid.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        formGrid.add(createFieldLabel("Login Password"), gbc);
        gbc.gridx = 1;
        passwordField = new ModernPasswordField("At least 6 characters", 15);
        formGrid.add(passwordField, gbc);

        // Phone Number
        gbc.gridx = 0; gbc.gridy = 3;
        formGrid.add(createFieldLabel("Phone Number"), gbc);
        gbc.gridx = 1;
        phoneField = new ModernTextField("+91 9876543210", 15);
        formGrid.add(phoneField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formGrid.add(createFieldLabel("Residential Address"), gbc);
        gbc.gridx = 1;
        addressField = new ModernTextField("City, State, Zip Code", 15);
        formGrid.add(addressField, gbc);

        // Account Type
        gbc.gridx = 0; gbc.gridy = 5;
        formGrid.add(createFieldLabel("Account Variant"), gbc);
        gbc.gridx = 1;
        accountTypeCombo = new JComboBox<>(AccountType.values());
        accountTypeCombo.setFont(UITheme.FONT_BODY);
        accountTypeCombo.setBackground(Color.WHITE);
        accountTypeCombo.addActionListener(e -> updatePolicyLabel());
        formGrid.add(accountTypeCombo, gbc);

        // Policy Info
        gbc.gridx = 1; gbc.gridy = 6;
        policyInfoLabel = new JLabel();
        policyInfoLabel.setFont(UITheme.FONT_SMALL);
        policyInfoLabel.setForeground(UITheme.ACCENT_GOLD.darker());
        formGrid.add(policyInfoLabel, gbc);

        // Initial Deposit
        gbc.gridx = 0; gbc.gridy = 7;
        formGrid.add(createFieldLabel("Initial Deposit (" + CurrencyFormatter.getCurrencySymbol() + ")"), gbc);
        gbc.gridx = 1;
        depositField = new ModernTextField("1000.00", 15);
        formGrid.add(depositField, gbc);

        // 4-Digit Security PIN
        gbc.gridx = 0; gbc.gridy = 8;
        formGrid.add(createFieldLabel("4-Digit Security PIN"), gbc);
        gbc.gridx = 1;
        pinField = new ModernPasswordField("4-digit numeric PIN", 15);
        formGrid.add(pinField, gbc);

        // Confirm Security PIN
        gbc.gridx = 0; gbc.gridy = 9;
        formGrid.add(createFieldLabel("Confirm 4-Digit PIN"), gbc);
        gbc.gridx = 1;
        confirmPinField = new ModernPasswordField("Repeat 4-digit PIN", 15);
        formGrid.add(confirmPinField, gbc);

        // Error message label
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER_RED);
        formGrid.add(errorLabel, gbc);

        updatePolicyLabel();
        card.add(formGrid, BorderLayout.CENTER);

        // 3. Footer Action Buttons
        JPanel footerPanel = new JPanel(new BorderLayout(0, 8));
        footerPanel.setOpaque(false);

        ModernButton submitBtn = new ModernButton("Open Account Now", ModernButton.ButtonType.PRIMARY);
        submitBtn.setFont(UITheme.FONT_BODY_BOLD);
        submitBtn.addActionListener(e -> performRegistration());
        footerPanel.add(submitBtn, BorderLayout.NORTH);

        JPanel backBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backBar.setOpaque(false);
        JLabel hasAccLabel = new JLabel("Already registered? ");
        hasAccLabel.setFont(UITheme.FONT_BODY);
        hasAccLabel.setForeground(UITheme.TEXT_SECONDARY);

        JButton loginLink = new JButton("Sign In Here");
        loginLink.setFont(UITheme.FONT_BODY_BOLD);
        loginLink.setForeground(UITheme.PRIMARY_NAVY);
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setFocusPainted(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> {
            clearFields();
            if (onNavigateLogin != null) onNavigateLogin.run();
        });

        backBar.add(hasAccLabel);
        backBar.add(loginLink);
        footerPanel.add(backBar, BorderLayout.SOUTH);

        card.add(footerPanel, BorderLayout.SOUTH);
        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void updatePolicyLabel() {
        AccountType selected = (AccountType) accountTypeCombo.getSelectedItem();
        if (selected == AccountType.CURRENT) {
            policyInfoLabel.setText("Min balance: ₹500.00 | Business transactions | 0% interest");
            if (depositField.getText().isEmpty() || depositField.getText().equals("1000.00")) {
                depositField.setText("500.00");
            }
        } else {
            policyInfoLabel.setText("Min balance: ₹1,000.00 | 4.0% p.a. interest accrual");
            if (depositField.getText().isEmpty() || depositField.getText().equals("500.00")) {
                depositField.setText("1000.00");
            }
        }
    }

    private void performRegistration() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        AccountType type = (AccountType) accountTypeCombo.getSelectedItem();
        String depositText = depositField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || pin.isEmpty()) {
            errorLabel.setText("Please fill in all mandatory fields.");
            return;
        }

        if (!pin.equals(confirmPin)) {
            errorLabel.setText("Security PIN and Confirmation PIN do not match.");
            return;
        }

        double initialDeposit;
        try {
            initialDeposit = Double.parseDouble(depositText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Initial deposit must be a valid numerical value.");
            return;
        }

        try {
            errorLabel.setText(" ");
            Customer customer = authService.registerCustomer(
                    name, email, password, phone, address, type, initialDeposit, pin
            );

            JOptionPane.showMessageDialog(this,
                    "🎉 Account Created Successfully!\n\n" +
                    "Welcome to Apex Digital Bank, " + customer.getFullName() + ".\n" +
                    "Customer Code: " + customer.getCustomerCode() + "\n" +
                    "Please sign in with your credentials to access your dashboard.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            if (onRegisterSuccess != null) {
                onRegisterSuccess.accept(customer);
            }
        } catch (BankingException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    public void clearFields() {
        fullNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        phoneField.setText("");
        addressField.setText("");
        pinField.setText("");
        confirmPinField.setText("");
        depositField.setText("1000.00");
        errorLabel.setText(" ");
    }
}
