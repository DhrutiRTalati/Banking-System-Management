package com.banking.gui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Design system and color palette for the Apex Digital Banking System.
 * Themes: Professional Deep Navy Blue, Pure White, Soft Slate, Subtle Gold,
 * Emerald Green (Credits/Success), and Crimson Red (Debits/Errors).
 */
public final class UITheme {

    private UITheme() {}

    // --- Core Color Palette ---
    public static final Color PRIMARY_NAVY       = new Color(15, 32, 67);      // #0F2043 (Deep Banking Navy)
    public static final Color SECONDARY_NAVY     = new Color(26, 54, 93);      // #1A365D (Medium Slate Navy)
    public static final Color NAVY_HOVER         = new Color(30, 64, 112);     // Hover state
    public static final Color ACCENT_GOLD        = new Color(212, 175, 55);    // #D4AF37 (Subtle Premium Gold)
    public static final Color ACCENT_GOLD_HOVER  = new Color(229, 193, 78);
    public static final Color BG_MAIN            = new Color(248, 250, 252);   // #F8FAFC (Soft Clean Background)
    public static final Color BG_CARD            = Color.WHITE;                // #FFFFFF (Pure Card White)
    public static final Color BG_SIDEBAR         = new Color(10, 25, 47);      // #0A192F (Dark Navy Sidebar)
    public static final Color TEXT_PRIMARY       = new Color(15, 23, 42);      // #0F172A (Deep Slate Text)
    public static final Color TEXT_SECONDARY     = new Color(100, 116, 139);   // #64748B (Muted Gray Text)
    public static final Color TEXT_MUTED         = new Color(148, 163, 184);   // #94A3B8
    public static final Color BORDER_LIGHT       = new Color(226, 232, 240);   // #E2E8F0 (Soft Border)
    public static final Color BORDER_FOCUS       = new Color(37, 99, 235);     // #2563EB (Focus Ring)

    // Semantic Status Colors
    public static final Color SUCCESS_GREEN      = new Color(16, 185, 129);    // #10B981 (Credits, Active)
    public static final Color SUCCESS_BG         = new Color(236, 253, 245);
    public static final Color DANGER_RED         = new Color(239, 68, 68);     // #EF4444 (Debits, Frozen)
    public static final Color DANGER_BG          = new Color(254, 242, 242);
    public static final Color WARNING_AMBER      = new Color(245, 158, 11);    // #F59E0B
    public static final Color WARNING_BG         = new Color(254, 252, 232);

    // --- Typography ---
    public static final String FONT_FAMILY       = "Segoe UI";
    public static final Font FONT_TITLE          = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_HEADER         = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_SUBHEADER      = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY           = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD      = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL          = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_BALANCE        = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_CODE           = new Font("Consolas", Font.PLAIN, 13);

    // --- Shared Borders & Dimensions ---
    public static final EmptyBorder PADDING_CARD = new EmptyBorder(20, 20, 20, 20);
    public static final EmptyBorder PADDING_FORM = new EmptyBorder(12, 12, 12, 12);
    public static final Border BORDER_INPUT      = BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_LIGHT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
    );

    /**
     * Configures universal UI defaults for clean font and anti-aliasing rendering.
     */
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY_BOLD);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_SUBHEADER);
    }
}
