package com.banking.gui.components;

import com.banking.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * A rounded white card panel providing clean depth and containment for dashboards and forms.
 */
public class CardPanel extends JPanel {
    private final int cornerRadius;
    private Color customBackground;
    private Color borderColor;

    public CardPanel(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.customBackground = UITheme.BG_CARD;
        this.borderColor = UITheme.BORDER_LIGHT;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public CardPanel() {
        this(12);
    }

    public CardPanel(LayoutManager layout) {
        this(12);
        setLayout(layout);
    }

    public void setCustomBackground(Color color) {
        this.customBackground = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Card background fill
        g2.setColor(customBackground);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Card border stroke
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
