package com.cosplay.util;

import com.cosplay.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to migrate existing plain-text passwords to BCrypt hashed passwords.
 * This should be run once to upgrade the database security.
 * 
 * WARNING: This assumes you know the plain-text passwords. 
 * In production, you should require users to reset their passwords.
 */
public class PasswordMigration {
    
    /**
     * Migrate all users with plain-text passwords to BCrypt hashed passwords.
     * This is a one-time migration utility.
     */
    public static void migratePasswords() {
        List<User> users = getAllUsers();
        int migrated = 0;
        
        for (User user : users) {
            String password = user.getPassword();
            
            // Check if password is already hashed (BCrypt hashes start with $2a$ or $2b$)
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                // This is a plain-text password, hash it
                String hashedPassword = PasswordUtil.hashPassword(password);
                if (updateUserPassword(user.getUserId(), hashedPassword)) {
                    migrated++;
                    System.out.println("Migrated password for user: " + user.getUsername());
                } else {
                    System.err.println("Failed to migrate password for user: " + user.getUsername());
                }
            }
        }
        
        System.out.println("\nPassword migration complete. Migrated " + migrated + " user(s).");
    }
    
    /**
     * Get all users from database.
     */
    private static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Update user password in database.
     */
    private static boolean updateUserPassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Main method to run the migration.
     */
    public static void main(String[] args) {
        System.out.println("Starting password migration...");
        System.out.println("This will hash all plain-text passwords in the database.\n");
        
        migratePasswords();
    }
}
