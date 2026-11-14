package com.banking.gui.components;

import com.banking.gui.theme.UITheme;
import com.banking.model.AccountStatus;

import javax.swing.*;
import java.awt.*;

/**
 * A styled chip / pill badge component for status indicators (ACTIVE, FROZEN, CLOSED).
 */
public class StatusBadge extends JLabel {
    private Color bgColor;
    private Color textColor;
    private int cornerRadius = 12;

    public StatusBadge(AccountStatus status) {
        super(status != null ? status.getLabel().toUpperCase() : "UNKNOWN", SwingConstants.CENTER);
        setStatus(status);
        setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    }

    public StatusBadge(String text, Color bgColor, Color textColor) {
        super(text, SwingConstants.CENTER);
        this.bgColor = bgColor;
        this.textColor = textColor;
        setForeground(textColor);
        setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    }

    public void setStatus(AccountStatus status) {
        if (status == AccountStatus.ACTIVE) {
            this.bgColor = UITheme.SUCCESS_BG;
            this.textColor = UITheme.SUCCESS_GREEN;
            setText("● ACTIVE");
        } else if (status == AccountStatus.FROZEN) {
            this.bgColor = UITheme.DANGER_BG;
            this.textColor = UITheme.DANGER_RED;
            setText("■ FROZEN");
        } else {
            this.bgColor = new Color(241, 245, 249);
            this.textColor = UITheme.TEXT_SECONDARY;
            setText("○ CLOSED");
        }
        setForeground(textColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Pill background
        g2.setColor(bgColor != null ? bgColor : UITheme.SUCCESS_BG);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Border stroke
        g2.setColor(textColor != null ? textColor : UITheme.SUCCESS_GREEN);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
