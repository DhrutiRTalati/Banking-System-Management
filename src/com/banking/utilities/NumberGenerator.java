package com.banking.utilities;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generator for bank account numbers, transaction IDs, and customer reference codes.
 */
public final class NumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private NumberGenerator() {}

    /**
     * Generates a 10-digit unique bank account number starting with standard bank prefix (e.g., 100).
     */
    public static long generateAccountNumber() {
        // 100XXXXXXX (10-digit number)
        long lower = 1000000000L;
        long upper = 9999999999L;
        long range = upper - lower + 1;
        long randomOffset = Math.abs(RANDOM.nextLong()) % range;
        return lower + randomOffset;
    }

    /**
     * Generates a unique transaction reference formatted as: TXN-YYYYMMDD-XXXXXX
     */
    public static String generateTransactionReference() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        int randomPart = 100000 + RANDOM.nextInt(900000);
        return "TXN-" + datePart + "-" + randomPart;
    }

    /**
     * Generates a customer identifier code (e.g. CUST-48291).
     */
    public static String generateCustomerCode() {
        int code = 10000 + RANDOM.nextInt(90000);
        return "CUST-" + code;
    }
}
