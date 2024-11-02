package ims;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class Account {
    private DatabaseAPI db;
    private static final String PEPPER_FILE_PATH = "data/pepper.txt";
    private static String pepper;
    private int failedAttempts = 0;  // Track failed login attempts
    private static final int MAX_FAILED_ATTEMPTS = 3;  // Max allowed failed attempts

    public Account() {
        db = new DatabaseAPI();
        loadOrGeneratePepper();
    }

    private void loadOrGeneratePepper() {
        try {
            File pepperFile = new File(PEPPER_FILE_PATH);
            if (!pepperFile.exists()) {
                pepper = generatePepper();
                savePepperToFile(pepper);
                System.out.println("New pepper generated and saved.");
            } else {
                pepper = new String(Files.readAllBytes(Paths.get(PEPPER_FILE_PATH)));
                System.out.println("Pepper loaded from file.");
            }
        } catch (IOException e) {
            System.out.println("Error loading or generating pepper: " + e.getMessage());
        }
    }

    private String generatePepper() {
        SecureRandom sr = new SecureRandom();
        byte[] pepperBytes = new byte[16];
        sr.nextBytes(pepperBytes);
        return Base64.getEncoder().encodeToString(pepperBytes);
    }

    private void savePepperToFile(String pepper) throws IOException {
        File dataDirectory = new File("data");
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        try (FileWriter writer = new FileWriter(PEPPER_FILE_PATH)) {
            writer.write(pepper);
        }
    }

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String saltedPassword = password + pepper + salt;
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
        if (isAccountLocked()) {
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

    // Getter for failed attempts
    public int getFailedAttempts() {
        return failedAttempts;
    }

    // Check if account is locked
    public boolean isAccountLocked() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }
}
