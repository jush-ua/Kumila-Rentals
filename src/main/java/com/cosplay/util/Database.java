package com.cosplay.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:cosplay.db";
    private static HikariDataSource dataSource;

    static {
        // Initialize connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setMaximumPoolSize(10); // Limit concurrent connections for SQLite
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // SQLite specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        dataSource = new HikariDataSource(config);
    }

    public static Connection connect() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
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

        String createEventBanners =
            "CREATE TABLE IF NOT EXISTS event_banners (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "message TEXT NOT NULL," +
            "is_active INTEGER DEFAULT 0," +
            "background_color TEXT DEFAULT '#fff4ed'," +
            "text_color TEXT DEFAULT '#d47f47'," +
            "link_url TEXT," +
            "link_text TEXT," +
            "image_path TEXT," +
            "subtitle TEXT," +
            "event_name TEXT," +
            "venue TEXT," +
            "onsite_rent_date TEXT" +
            ");";

        String createMessages =
            "CREATE TABLE IF NOT EXISTS messages (" +
            "message_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "conversation_id INTEGER NOT NULL," +
            "sender_id INTEGER NOT NULL," +
            "sender_name TEXT NOT NULL," +
            "sender_email TEXT," +
            "message TEXT NOT NULL," +
            "timestamp TEXT NOT NULL," +
            "is_admin_reply INTEGER DEFAULT 0," +
            "status TEXT DEFAULT 'unread'," +
            "FOREIGN KEY(sender_id) REFERENCES users(user_id)" +
            ");";

        String createConversations =
            "CREATE TABLE IF NOT EXISTS conversations (" +
            "conversation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "user_name TEXT NOT NULL," +
            "user_email TEXT," +
            "last_message TEXT," +
            "last_message_time TEXT," +
            "unread_count INTEGER DEFAULT 0," +
            "created_at TEXT NOT NULL," +
            "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
            ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Drop old messages table if it has the wrong schema (subject column)
            try {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(messages)");
                boolean hasSubject = false;
                while (rs.next()) {
                    if ("subject".equals(rs.getString("name"))) {
                        hasSubject = true;
                        break;
                    }
                }
                if (hasSubject) {
                    stmt.executeUpdate("DROP TABLE IF EXISTS messages");
                    System.out.println("Dropped old messages table with incompatible schema.");
                }
            } catch (SQLException ignored) {
                // Table doesn't exist yet
            }
            
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
            stmt.execute(createEventBanners);
            stmt.execute(createConversations);
            stmt.execute(createMessages);
            
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
            
            // Add new rental columns if they don't exist
            try {
                stmt.executeUpdate("ALTER TABLE rentals ADD COLUMN rent_days INTEGER DEFAULT 1");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE rentals ADD COLUMN customer_addons TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE rentals ADD COLUMN selfie_photo TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE rentals ADD COLUMN id_photo TEXT");
            } catch (SQLException ignored) { }
            
            // Add conversation_id to messages table if it doesn't exist (for older databases)
            try {
                stmt.executeUpdate("ALTER TABLE messages ADD COLUMN conversation_id INTEGER");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE messages ADD COLUMN is_admin_reply INTEGER DEFAULT 0");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE messages ADD COLUMN status TEXT DEFAULT 'unread'");
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

            // Add new columns to event_banners table if they don't exist
            try {
                stmt.executeUpdate("ALTER TABLE event_banners ADD COLUMN image_path TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE event_banners ADD COLUMN subtitle TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE event_banners ADD COLUMN event_name TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE event_banners ADD COLUMN venue TEXT");
            } catch (SQLException ignored) { }
            try {
                stmt.executeUpdate("ALTER TABLE event_banners ADD COLUMN onsite_rent_date TEXT");
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
    
    /**
     * Load seed data from SQL file. This is useful for sharing sample data via repository.
     * Call this method after init() if you want to populate the database with test data.
     * 
     * Usage: Database.loadSeedData();
     */
    public static void loadSeedData() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             InputStream is = Database.class.getResourceAsStream("/db/seed_data.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                System.out.println("No seed data file found at /db/seed_data.sql");
                return;
            }
            
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sql.append(line).append(" ");
                
                // Execute when we hit a semicolon (end of statement)
                if (line.endsWith(";")) {
                    try {
                        stmt.execute(sql.toString());
                    } catch (SQLException e) {
                        System.err.println("Error executing seed statement: " + e.getMessage());
                    }
                    sql.setLength(0); // Clear for next statement
                }
            }
            
            System.out.println("Seed data loaded successfully.");
            
        } catch (Exception e) {
            System.err.println("Failed to load seed data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Export current database data to SQL format (for sharing).
     * This creates INSERT statements that can be committed to the repository.
     * The output is saved to src/main/resources/db/seed_data.sql
     */
    public static void exportAllData() {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Seed data for cosplay rental application\n");
        sql.append("-- Auto-generated export from database\n");
        sql.append("-- This file can be safely committed to version control\n");
        sql.append("-- Note: Users and rentals are NOT exported (user accounts are device-specific)\n\n");
        
        try (Connection conn = connect()) {
            // Export Cosplays
            sql.append("-- Cosplays\n");
            var cosplaysStmt = conn.createStatement();
            var cosplaysRs = cosplaysStmt.executeQuery("SELECT * FROM cosplays");
            while (cosplaysRs.next()) {
                sql.append(String.format("INSERT OR IGNORE INTO cosplays (cosplay_id, name, category, series_name, size, description, image_path, rent_rate_1day, rent_rate_2days, rent_rate_3days, add_ons) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %.2f, %.2f, %.2f, %s);\n",
                    cosplaysRs.getInt("cosplay_id"),
                    escapeSql(cosplaysRs.getString("name")),
                    escapeSql(cosplaysRs.getString("category")),
                    escapeSql(cosplaysRs.getString("series_name")),
                    escapeSql(cosplaysRs.getString("size")),
                    escapeSql(cosplaysRs.getString("description")),
                    escapeSql(cosplaysRs.getString("image_path")),
                    cosplaysRs.getDouble("rent_rate_1day"),
                    cosplaysRs.getDouble("rent_rate_2days"),
                    cosplaysRs.getDouble("rent_rate_3days"),
                    cosplaysRs.getString("add_ons") != null ? "'" + escapeSql(cosplaysRs.getString("add_ons")) + "'" : "NULL"));
            }
            sql.append("\n");
            
            // Export Featured Images
            sql.append("-- Featured Images\n");
            var featuredStmt = conn.createStatement();
            var featuredRs = featuredStmt.executeQuery("SELECT * FROM featured_images WHERE image_url IS NOT NULL");
            while (featuredRs.next()) {
                sql.append(String.format("INSERT OR IGNORE INTO featured_images (slot, image_url, title, cosplay_id) VALUES (%d, '%s', '%s', %s);\n",
                    featuredRs.getInt("slot"),
                    escapeSql(featuredRs.getString("image_url")),
                    escapeSql(featuredRs.getString("title")),
                    featuredRs.getObject("cosplay_id") != null ? featuredRs.getInt("cosplay_id") : "NULL"));
            }
            sql.append("\n");
            
            // Export Event Banners
            sql.append("-- Event Banners\n");
            var bannersStmt = conn.createStatement();
            var bannersRs = bannersStmt.executeQuery("SELECT * FROM event_banners");
            while (bannersRs.next()) {
                sql.append(String.format("INSERT OR IGNORE INTO event_banners (id, title, message, is_active, background_color, text_color, subtitle, image_path, event_name, venue, onsite_rent_date) VALUES (%d, '%s', '%s', %d, '%s', '%s', %s, %s, %s, %s, %s);\n",
                    bannersRs.getInt("id"),
                    escapeSql(bannersRs.getString("title")),
                    escapeSql(bannersRs.getString("message")),
                    bannersRs.getInt("is_active"),
                    escapeSql(bannersRs.getString("background_color")),
                    escapeSql(bannersRs.getString("text_color")),
                    toSqlString(bannersRs.getString("subtitle")),
                    toSqlString(bannersRs.getString("image_path")),
                    toSqlString(bannersRs.getString("event_name")),
                    toSqlString(bannersRs.getString("venue")),
                    toSqlString(bannersRs.getString("onsite_rent_date"))));
            }
            
            // Write to file
            String outputPath = "src/main/resources/db/seed_data.sql";
            java.nio.file.Files.writeString(java.nio.file.Paths.get(outputPath), sql.toString());
            System.out.println("✓ Database exported to " + outputPath);
            System.out.println("You can now commit this file to Git!");
            
        } catch (Exception e) {
            System.err.println("Failed to export data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }
    
    private static String toSqlString(String value) {
        return value != null ? "'" + escapeSql(value) + "'" : "NULL";
    }
}

