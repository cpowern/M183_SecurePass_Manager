package ims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseAPI {
    protected final String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/data/db.sqlite";

    public DatabaseAPI() {
        try {
            // Force load the SQLite driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found. Make sure the driver is added to your project.");
            e.printStackTrace();
        }

        // Initialize the database with simplified tables
        initializeDatabase();
        createTables();
    }

    // Initialize the database (dummy method, included to resolve error)
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Database initialized successfully at: " + url);
            }
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    // Create the necessary tables with correct structure
    private void createTables() {
        dropTableIfExists("Users"); // Drop the Users table if it exists
        createTable("Users", 
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR UNIQUE NOT NULL, " +
                    "email VARCHAR UNIQUE NOT NULL, " +
                    "password_hash VARCHAR NOT NULL, " +
                    "salt VARCHAR NOT NULL");
    }

    // Method to drop a table if it exists
    private void dropTableIfExists(String tableName) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String sql = "DROP TABLE IF EXISTS " + tableName;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                System.out.println("Table " + tableName + " dropped successfully if it existed.");
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error dropping table " + tableName + ": " + e.getMessage());
        }
    }

    // Method to create a table
    public void createTable(String tableName, String fields) {  
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n " + fields + ");";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                System.out.println("A new table " + tableName + " has been created.");
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error creating table " + tableName + ": " + e.getMessage());
        }
    }

    // Insert data into a table
    public void insert(String tableName, String fields, String values) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {            
                conn.setAutoCommit(false);
                Statement stmt = conn.createStatement();
                String sql = "INSERT INTO " + tableName + "("  + fields + ") VALUES (" + values +")";
                stmt.executeUpdate(sql);

                stmt.close();
                conn.commit();
            }
            System.out.println("Insert in " + tableName + " is done");
            
        } catch (SQLException e) {
            System.out.println("Error inserting into " + tableName + ": " + e.getMessage());
        } 
    }

    // Retrieve a value from a table
    public String getValue(String tableName, String keyName, String keyValue, String fieldName) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {            
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " == " + keyValue;
                ResultSet rs = stmt.executeQuery(sql);
                try {
                    String exS = rs.getString(fieldName);
                    stmt.close();
                    return exS;
                } catch (SQLException e) {
                    System.out.println("Error retrieving value from " + tableName + ": " + e.getMessage());
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
        return null;
    }

    // Check if a key exists in a table
    public boolean isKeyAvailable(String tableName, String keyName, String keyValue) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {  
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " == " + keyValue; 
                ResultSet rs = stmt.executeQuery(sql);
                try {
                    String exS = rs.getString(keyName);       
                    System.out.println("Key value " + exS + " from table " + tableName + " exists.");
                    stmt.close();
                    return true;
                } catch (SQLException e) {
                    System.out.println("Key value " + keyValue + " from table " + tableName + " does not exist.");
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
        return false;
    }
}
