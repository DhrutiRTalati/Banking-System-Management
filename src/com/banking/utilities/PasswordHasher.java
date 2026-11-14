package com.banking.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cryptographic utility providing SHA-256 password and PIN hashing.
 * Plaintext passwords and PINs are never stored in the database.
 */
public final class PasswordHasher {

    private PasswordHasher() {
        // Private constructor for static utility class
    }

    /**
     * Hashes an input string (password or PIN) using SHA-256.
     *
     * @param input the plaintext input to hash
     * @return 64-character hexadecimal SHA-256 digest
     */
    public static String hash(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Cannot hash null or empty input");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available on this JVM", e);
        }
    }

    /**
     * Securely compares plaintext against an existing SHA-256 hash in constant time
     * to mitigate timing attacks.
     */
    public static boolean verify(String plaintext, String storedHash) {
        if (plaintext == null || storedHash == null) {
            return false;
        }
        String computedHash = hash(plaintext);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
