package com.banking.gui.components;

import com.banking.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern styled JButton supporting variants, rounded corners, and hover transitions.
 */
public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY, SECONDARY, SUCCESS, DANGER, GOLD, OUTLINE
    }

    private final ButtonType type;
    private boolean isHovered = false;
    private int cornerRadius = 8;

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type != null ? type : ButtonType.PRIMARY;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(UITheme.FONT_BODY_BOLD);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 20, 10, 20));

        setupColors();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public ModernButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void setupColors() {
        switch (type) {
            case PRIMARY:
                setForeground(Color.WHITE);
                break;
            case SECONDARY:
                setForeground(Color.WHITE);
                break;
            case SUCCESS:
                setForeground(Color.WHITE);
                break;
            case DANGER:
                setForeground(Color.WHITE);
                break;
            case GOLD:
                setForeground(UITheme.TEXT_PRIMARY);
                break;
            case OUTLINE:
                setForeground(UITheme.PRIMARY_NAVY);
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg = getBackgroundColor();
        if (!isEnabled()) {
            bg = UITheme.TEXT_MUTED;
        }

        if (type == ButtonType.OUTLINE) {
            if (isHovered && isEnabled()) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            }
            g2.setColor(isEnabled() ? UITheme.PRIMARY_NAVY : UITheme.TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    private Color getBackgroundColor() {
        switch (type) {
            case PRIMARY:
                return isHovered ? UITheme.NAVY_HOVER : UITheme.PRIMARY_NAVY;
            case SECONDARY:
                return isHovered ? UITheme.SECONDARY_NAVY.brighter() : UITheme.SECONDARY_NAVY;
            case SUCCESS:
                return isHovered ? new Color(13, 148, 136) : UITheme.SUCCESS_GREEN;
            case DANGER:
                return isHovered ? new Color(220, 38, 38) : UITheme.DANGER_RED;
            case GOLD:
                return isHovered ? UITheme.ACCENT_GOLD_HOVER : UITheme.ACCENT_GOLD;
            case OUTLINE:
            default:
                return Color.WHITE;
        }
    }
}
