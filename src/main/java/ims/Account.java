package ims;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class Account {
    private DatabaseAPI db;

    public Account() {
        db = new DatabaseAPI();
    }

    // Methode zum Generieren von Salt
    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Passwort-Hashing mit PBKDF2
    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 10000, 512);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    // Updated verifyPassword to accept three arguments for verification
    private boolean verifyPassword(String originalPassword, String storedHash, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String newHash = hashPassword(originalPassword, salt);
        return newHash.equals(storedHash);
    }

// Benutzerkonto hinzufügen (ohne interne Überprüfung)
public void addAccount(String username, String password) {
    try {
        // Do NOT check for existing users here. Verification happens only in the controller.

        // Generiere Salt und hashe das Passwort
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        // Speichere Benutzerinformationen in der Users-Tabelle
        db.insert("Users", "username, email, password_hash, salt", 
                  "'" + username + "', '" + username + "@example.com', '" + hashedPassword + "', '" + salt + "'");
        System.out.println("User " + username + " successfully registered.");

    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        System.out.println("Error while hashing the password: " + e.getMessage());
    }
}



    // Verifiziere das Passwort des Benutzers
    public boolean verifyPassword(String username, String password) {
        try {
            // Hole das gehashte Passwort und das Salt aus der Users-Tabelle
            String storedHash = db.getValue("Users", "username", "'" + username + "'", "password_hash");
            String storedSalt = db.getValue("Users", "username", "'" + username + "'", "salt");

            // Überprüfe, ob das gehashte Passwort und das Salt null sind
            if (storedHash == null || storedSalt == null) {
                System.out.println("Error: Stored hash or salt is null for user: " + username);
                return false;
            }

            // Verifiziere das Passwort mit drei Argumenten
            return verifyPassword(password, storedHash, storedSalt);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error while verifying the password: " + e.getMessage());
            return false;
        }
    }

public boolean verifyAccount(String username) {
    System.out.println("[DEBUG] verifyAccount called for: " + username);
    boolean exists = db.isKeyAvailable("Users", "username", "'" + username + "'");
    
    // Log only if the user exists, to reduce redundant log messages
    if (exists) {
        System.out.println("Key value '" + username + "' from table Users exists.");
    }

    return exists;
}


}
