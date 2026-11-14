package com.banking.gui.panels;

import com.banking.exceptions.BankingException;
import com.banking.gui.components.CardPanel;
import com.banking.gui.components.ModernButton;
import com.banking.gui.components.ModernPasswordField;
import com.banking.gui.components.ModernTextField;
import com.banking.gui.theme.UITheme;
import com.banking.model.User;
import com.banking.services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern login panel supporting single-click demo credential filling,
 * real-time validation, and responsive split-card layout.
 */
public class LoginPanel extends JPanel {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onNavigateRegister;

    private ModernTextField emailField;
    private ModernPasswordField passwordField;
    private JLabel errorLabel;

    public LoginPanel(AuthService authService, Consumer<User> onLoginSuccess, Runnable onNavigateRegister) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateRegister = onNavigateRegister;

        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_MAIN);

        // Center Login Card
        CardPanel loginCard = new CardPanel(16);
        loginCard.setPreferredSize(new Dimension(460, 560));
        loginCard.setLayout(new BorderLayout(0, 16));
        loginCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // 1. Branding Header
        JPanel brandPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🏦 APEX DIGITAL BANK", SwingConstants.CENTER);
        logoLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 22));
        logoLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Enterprise Core Banking System", SwingConstants.CENTER);
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel signinLabel = new JLabel("Sign In to Your Account", SwingConstants.CENTER);
        signinLabel.setFont(UITheme.FONT_HEADER);
        signinLabel.setForeground(UITheme.TEXT_PRIMARY);

        brandPanel.add(logoLabel);
        brandPanel.add(subLabel);
        brandPanel.add(signinLabel);
        loginCard.add(brandPanel, BorderLayout.NORTH);

        // 2. Form Fields Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Email Label & Input
        gbc.gridy = 0;
        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(UITheme.FONT_BODY_BOLD);
        emailLbl.setForeground(UITheme.TEXT_PRIMARY);
        formPanel.add(emailLbl, gbc);

        gbc.gridy = 1;
        emailField = new ModernTextField("name@example.com", 20);
        formPanel.add(emailField, gbc);

        // Password Label & Input
        gbc.gridy = 2;
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        passLbl.setForeground(UITheme.TEXT_PRIMARY);
        formPanel.add(passLbl, gbc);

        gbc.gridy = 3;
        passwordField = new ModernPasswordField("Enter your password", 20);
        passwordField.addActionListener(e -> performLogin());
        formPanel.add(passwordField, gbc);

        // Error message label
        gbc.gridy = 4;
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER_RED);
        formPanel.add(errorLabel, gbc);

        // Sign In Button
        gbc.gridy = 5;
        ModernButton loginBtn = new ModernButton("Sign In", ModernButton.ButtonType.PRIMARY);
        loginBtn.setFont(UITheme.FONT_BODY_BOLD);
        loginBtn.addActionListener(e -> performLogin());
        formPanel.add(loginBtn, gbc);

        // Demo Quick Fill Buttons
        gbc.gridy = 6;
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoBar.setOpaque(false);

        ModernButton demoAdminBtn = new ModernButton("Demo Admin", ModernButton.ButtonType.OUTLINE);
        demoAdminBtn.setFont(UITheme.FONT_SMALL);
        demoAdminBtn.setMargin(new Insets(4, 10, 4, 10));
        demoAdminBtn.addActionListener(e -> {
            emailField.setText("admin@bank.com");
            passwordField.setText("Admin@123");
            errorLabel.setText(" ");
        });

        demoBar.add(demoAdminBtn);
        formPanel.add(demoBar, gbc);

        loginCard.add(formPanel, BorderLayout.CENTER);

        // 3. Footer Navigation to Register
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);

        JLabel noAccLabel = new JLabel("New to Apex Bank? ");
        noAccLabel.setFont(UITheme.FONT_BODY);
        noAccLabel.setForeground(UITheme.TEXT_SECONDARY);

        JButton registerLink = new JButton("Open an Account");
        registerLink.setFont(UITheme.FONT_BODY_BOLD);
        registerLink.setForeground(UITheme.PRIMARY_NAVY);
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            clearFields();
            if (onNavigateRegister != null) onNavigateRegister.run();
        });

        footerPanel.add(noAccLabel);
        footerPanel.add(registerLink);
        loginCard.add(footerPanel, BorderLayout.SOUTH);

        add(loginCard);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            errorLabel.setText(" ");
            User user = authService.login(email, pass);
            clearFields();
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } catch (BankingException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
    }
}
