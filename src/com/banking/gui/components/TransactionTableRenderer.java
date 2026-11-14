package com.banking.gui.components;

import com.banking.gui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer providing alternating row backgrounds,
 * right-aligned currency formatting, and green/red semantic coloring for credits/debits.
 */
public class TransactionTableRenderer extends DefaultTableCellRenderer {

    private final int amountColumnIndex;
    private final int typeColumnIndex;

    public TransactionTableRenderer(int amountColumnIndex, int typeColumnIndex) {
        this.amountColumnIndex = amountColumnIndex;
        this.typeColumnIndex = typeColumnIndex;
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    public TransactionTableRenderer() {
        this(3, 2);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
        } else {
            c.setBackground(new Color(224, 231, 255));
        }

        // Alignments and font
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_PRIMARY);

        // Right-align numeric columns
        if (column == amountColumnIndex || column == amountColumnIndex + 1) {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(UITheme.FONT_BODY_BOLD);
        } else if (column == 0 || column == 1) {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(UITheme.FONT_CODE);
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        // Color coding for amount column based on credit vs debit
        if (column == amountColumnIndex && value != null) {
            String valStr = value.toString();
            if (valStr.startsWith("+")) {
                setForeground(UITheme.SUCCESS_GREEN);
            } else if (valStr.startsWith("-")) {
                setForeground(UITheme.DANGER_RED);
            }
        }

        return c;
    }
}
