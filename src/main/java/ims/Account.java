package ims;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class Account {
    private DatabaseAPI db;
    private static final String PEPPER = "YourSecretPepperValue";  // Add a fixed pepper for security
    private int failedAttempts = 0;  // Track failed login attempts
    private static final int MAX_FAILED_ATTEMPTS = 3;  // Max allowed failed attempts

    public Account() {
        db = new DatabaseAPI();
    }

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String saltedPassword = password + PEPPER + salt;
        PBEKeySpec spec = new PBEKeySpec(saltedPassword.toCharArray(), salt.getBytes(), 10000, 512);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public boolean addAccount(String username, String password) {
        if (!isPasswordStrong(password)) {
            System.out.println("Password does not meet the strength requirements.");
            return false;
        }
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            db.insert("Users", "username, email, password_hash",
                      "'" + username + "', '" + username + "@example.com', '" + hashedPassword + "'");

            String userId = db.getValue("Users", "username", "'" + username + "'", "user_id");
            if (userId != null) {
                db.insert("UserSalt", "user_id, salt", userId + ", '" + salt + "'");
            }

            return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error while hashing the password: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyPassword(String username, String password) {
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            System.out.println("Account is locked due to too many failed attempts.");
            return false;
        }

        try {
            String storedHash = db.getValue("Users", "username", "'" + username + "'", "password_hash");
            String userId = db.getValue("Users", "username", "'" + username + "'", "user_id");
            if (userId == null) return false;

            String storedSalt = db.getValue("UserSalt", "user_id", userId, "salt");

            if (storedHash == null || storedSalt == null) {
                System.out.println("Error: Stored hash or salt is null for user: " + username);
                return false;
            }

            String newHash = hashPassword(password, storedSalt);
            if (newHash.equals(storedHash)) {
                failedAttempts = 0;  // Reset failed attempts on successful login
                return true;
            } else {
                failedAttempts++;
                return false;
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error while verifying the password: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyAccount(String username) {
        return db.isKeyAvailable("Users", "username", "'" + username + "'");
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isAccountLocked() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }
}
