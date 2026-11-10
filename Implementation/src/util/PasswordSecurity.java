package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for cryptographic password hashing using SHA-256 with unique per-user salts.
 */
public class PasswordSecurity {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a 16-byte cryptographically secure random salt encoded in Base64.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Computes the SHA-256 hash of a plaintext password concatenated with the given salt.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in environment", e);
        }
    }

    /**
     * Verifies a candidate password against an existing hash and salt.
     */
    public static boolean verifyPassword(String candidatePassword, String salt, String expectedHash) {
        if (candidatePassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(candidatePassword, salt);
        return MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8),
                                     expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}

