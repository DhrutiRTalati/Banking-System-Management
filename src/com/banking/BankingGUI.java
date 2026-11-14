package com.banking;

import com.banking.gui.MainFrame;
import com.banking.gui.theme.UITheme;

import javax.swing.SwingUtilities;

/**
 * Graphical User Interface launcher for the Apex Digital Banking System.
 */
public class BankingGUI {

    public static void main(String[] args) {
        // Apply global styling, fonts, and anti-aliasing
        UITheme.applyGlobalTheme();

        // Launch on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Fatal GUI Initialization Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
