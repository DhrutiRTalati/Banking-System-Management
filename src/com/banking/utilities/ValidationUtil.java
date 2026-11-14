package com.banking.utilities;

import com.banking.exceptions.ValidationException;

import java.util.regex.Pattern;

/**
 * Validation utility for sanitizing and verifying user inputs across GUI and CLI layers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,14}$");

    private ValidationUtil() {}

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format (e.g. user@example.com).");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }

    public static void validatePin(String pin) throws ValidationException {
        if (pin == null || !PIN_PATTERN.matcher(pin.trim()).matches()) {
            throw new ValidationException("Security PIN must be exactly 4 numeric digits.");
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Phone number must contain 10-14 digits.");
        }
    }

    public static void validatePositiveAmount(double amount, String operation) throws ValidationException {
        if (amount <= 0.0) {
            throw new ValidationException("Amount for " + operation + " must be strictly positive.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new ValidationException("Invalid numerical amount provided.");
        }
    }

    public static void validateName(String name) throws ValidationException {
        if (name == null || name.trim().length() < 2) {
            throw new ValidationException("Full name must contain at least 2 characters.");
        }
    }
}
