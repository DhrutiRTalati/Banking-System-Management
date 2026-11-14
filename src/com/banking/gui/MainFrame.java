package com.banking.gui;

import com.banking.database.DatabaseInitializer;
import com.banking.gui.panels.AdminDashboardPanel;
import com.banking.gui.panels.CustomerDashboardPanel;
import com.banking.gui.panels.LoginPanel;
import com.banking.gui.panels.RegisterPanel;
import com.banking.gui.theme.UITheme;
import com.banking.model.Customer;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application frame containing the master CardLayout view switcher.
 */
public class MainFrame extends JFrame {

    public static final String CARD_LOGIN     = "LOGIN";
    public static final String CARD_REGISTER  = "REGISTER";
    public static final String CARD_CUSTOMER  = "CUSTOMER";
    public static final String CARD_ADMIN     = "ADMIN";

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AdminService adminService;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private CustomerDashboardPanel customerDashboard;
    private AdminDashboardPanel adminDashboard;

    public MainFrame() {
        super("Apex Digital Banking System");

        // Initialize Services
        this.authService = new AuthServiceImpl();
        this.accountService = new AccountServiceImpl();
        this.transactionService = new TransactionServiceImpl();
        this.adminService = new AdminServiceImpl();

        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);

        initDatabase();
        initUI();
    }

    private void initDatabase() {
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Database Initialization Warning:\n" + e.getMessage() +
                    "\n\nDefaulting to embedded SQLite engine.",
                    "Database Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        // Instantiate Screen Panels
        loginPanel = new LoginPanel(
                authService,
                this::handleLoginSuccess,
                () -> showCard(CARD_REGISTER)
        );

        registerPanel = new RegisterPanel(
                authService,
                customer -> showCard(CARD_LOGIN),
                () -> showCard(CARD_LOGIN)
        );

        customerDashboard = new CustomerDashboardPanel(
                accountService,
                transactionService,
                this::handleLogout
        );

        adminDashboard = new AdminDashboardPanel(
                adminService,
                this::handleLogout
        );

        cardsPanel.add(loginPanel, CARD_LOGIN);
        cardsPanel.add(registerPanel, CARD_REGISTER);
        cardsPanel.add(customerDashboard, CARD_CUSTOMER);
        cardsPanel.add(adminDashboard, CARD_ADMIN);

        getContentPane().add(cardsPanel, BorderLayout.CENTER);

        // Graceful cleanup on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AsyncAuditLogger.getInstance().shutdown();
            }
        });

        showCard(CARD_LOGIN);
    }

    public void showCard(String cardName) {
        cardLayout.show(cardsPanel, cardName);
    }

    private void handleLoginSuccess(User user) {
        if (user.getRole() == UserRole.ADMIN) {
            adminDashboard.setAdmin(user);
            showCard(CARD_ADMIN);
        } else {
            Customer customer;
            if (user instanceof Customer) {
                customer = (Customer) user;
            } else {
                customer = new Customer(user.getId(), user.getFullName(), user.getEmail(),
                        user.getPasswordHash(), "", "", "", user.getCreatedAt());
            }
            customerDashboard.setCustomer(customer);
            showCard(CARD_CUSTOMER);
        }
    }

    private void handleLogout() {
        loginPanel.clearFields();
        showCard(CARD_LOGIN);
    }
}
