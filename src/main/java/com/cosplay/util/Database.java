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
        String createCostumes =
            "CREATE TABLE IF NOT EXISTS costumes (" +
            "costume_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "category TEXT," +
            "size TEXT," +
            "description TEXT," +
            "image_path TEXT" +
            ");";

        String createRentals =
            "CREATE TABLE IF NOT EXISTS rentals (" +
            "rental_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "costume_id INTEGER NOT NULL," +
            "customer_name TEXT," +
            "contact_number TEXT," +
            "address TEXT," +
            "facebook_link TEXT," +
            "start_date TEXT NOT NULL," +   // YYYY-MM-DD
            "end_date TEXT NOT NULL," +
            "payment_method TEXT," +
            "proof_of_payment TEXT," +
            "status TEXT DEFAULT 'Pending'," +
            "FOREIGN KEY(costume_id) REFERENCES costumes(costume_id)" +
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
            stmt.execute(createCostumes);
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
            
            // Try to add costume_id column for linking featured slots to costumes
            try {
                stmt.executeUpdate("ALTER TABLE featured_images ADD COLUMN costume_id INTEGER REFERENCES costumes(costume_id)");
            } catch (SQLException ignored) {
                // Column may already exist; ignore
            }

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
