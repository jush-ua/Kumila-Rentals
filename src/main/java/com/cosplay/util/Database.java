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
            "password VARCHAR(255) NOT NULL," +
            "email VARCHAR(100)," +
            "role VARCHAR(20) DEFAULT 'customer'" +
            ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCostumes);
            stmt.execute(createRentals);
            stmt.execute(createUsers);
            System.out.println("Database initialized (cosplay.db).");
        } catch (SQLException e) {
            System.err.println("Failed to initialize DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
