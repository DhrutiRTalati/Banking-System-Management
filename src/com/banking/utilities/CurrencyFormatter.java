package com.banking.utilities;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for uniform monetary and numerical representations.
 */
public final class CurrencyFormatter {
    private static final String CURRENCY_SYMBOL = "₹";

    private CurrencyFormatter() {}

    public static String format(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return CURRENCY_SYMBOL + " " + nf.format(amount);
    }

    public static String formatWithoutSymbol(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
}
