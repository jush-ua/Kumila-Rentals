package com.cosplay.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:cosplay.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Called at app startup to create tables if they don't exist
    public static void init() {
        String createcosplays =
            "CREATE TABLE IF NOT EXISTS cosplays (" +
            "cosplay_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "category TEXT," +
            "series_name TEXT," +
            "size TEXT," +
            "description TEXT," +
            "image_path TEXT," +
            "rent_rate_1day REAL," +
            "rent_rate_2days REAL," +
            "rent_rate_3days REAL," +
            "add_ons TEXT" +
            ");";

        String createRentals =
            "CREATE TABLE IF NOT EXISTS rentals (" +
            "rental_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "cosplay_id INTEGER NOT NULL," +
            "customer_name TEXT," +
            "contact_number TEXT," +
            "address TEXT," +
            "facebook_link TEXT," +
            "start_date TEXT NOT NULL," +   // YYYY-MM-DD
            "end_date TEXT NOT NULL," +
            "payment_method TEXT," +
            "proof_of_payment TEXT," +
            "status TEXT DEFAULT 'Pending'," +
            "FOREIGN KEY(cosplay_id) REFERENCES cosplays(cosplay_id)" +
            ");";

        String createUsers =
            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username VARCHAR(50) UNIQUE NOT NULL," +
            "password VARCHAR(255)," +
            "email VARCHAR(100)," +
            "role VARCHAR(20) DEFAULT 'customer'," +
            "email_verified INTEGER DEFAULT 0," +
            "verification_token VARCHAR(255)," +
            "oauth_provider VARCHAR(20)," +
            "oauth_id VARCHAR(255)" +
            ");";

        String createFeatured =
            "CREATE TABLE IF NOT EXISTS featured_images (" +
            "slot INTEGER PRIMARY KEY," +
            "image_url TEXT," +
            "title TEXT" +
            ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Migrate old 'costumes' table to 'cosplays' if it exists
            try {
                stmt.executeUpdate("ALTER TABLE costumes RENAME TO cosplays");
                System.out.println("Migrated 'costumes' table to 'cosplays'.");
            } catch (SQLException ignored) {
                // Table doesn't exist or already renamed
            }
            
            // Migrate old 'costume_id' column in rentals table if it exists
            try {
                stmt.executeUpdate("ALTER TABLE rentals RENAME COLUMN costume_id TO cosplay_id");
                System.out.println("Migrated 'costume_id' column to 'cosplay_id' in rentals.");
            } catch (SQLException ignored) {
                // Column doesn't exist or already renamed
            }
            
            // Migrate old 'costume_id' column in featured_images table if it exists
            try {
                stmt.executeUpdate("ALTER TABLE featured_images RENAME COLUMN costume_id TO cosplay_id");
                System.out.println("Migrated 'costume_id' column to 'cosplay_id' in featured_images.");
            } catch (SQLException ignored) {
                // Column doesn't exist or already renamed
            }
            
            stmt.execute(createcosplays);
            stmt.execute(createRentals);
            stmt.execute(createUsers);
            stmt.execute(createFeatured);
            
            // Add new columns if they don't exist (for existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN email_verified INTEGER DEFAULT 0");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN verification_token VARCHAR(255)");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(20)");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN oauth_id VARCHAR(255)");
            } catch (SQLException ignored) { }
            
            // Try to add cosplay_id column for linking featured slots to cosplays
            try {
                stmt.executeUpdate("ALTER TABLE featured_images ADD COLUMN cosplay_id INTEGER REFERENCES cosplays(cosplay_id)");
            } catch (SQLException ignored) {
                // Column may already exist; ignore
            }
            
            // Add new columns to cosplays table if they don't exist
            try {
                stmt.executeUpdate("ALTER TABLE cosplays ADD COLUMN series_name TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE cosplays ADD COLUMN rent_rate_1day REAL");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE cosplays ADD COLUMN rent_rate_2days REAL");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE cosplays ADD COLUMN rent_rate_3days REAL");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE cosplays ADD COLUMN add_ons TEXT");
            } catch (SQLException ignored) { }

            // Seed a default admin account (username: admin, password: admin)
            // Hash the password using BCrypt
            String hashedPassword = PasswordUtil.hashPassword("admin");
            stmt.executeUpdate("INSERT OR IGNORE INTO users(username,password,email,role) VALUES('admin','" + hashedPassword + "','admin@example.com','admin')");
            // Seed 4 featured slots if absent
            for (int i = 1; i <= 4; i++) {
                stmt.executeUpdate("INSERT OR IGNORE INTO featured_images(slot,image_url,title) VALUES(" + i + ", NULL, NULL)");
            }
            System.out.println("Database initialized (cosplay.db).");
        } catch (SQLException e) {
            System.err.println("Failed to initialize DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

