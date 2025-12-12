package com.cosplay.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    /**
     * Validate username format.
     * @param username the username to validate
     * @return error message if invalid, null if valid
     */
    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        
        username = username.trim();
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "Username must be at least " + MIN_USERNAME_LENGTH + " characters";
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "Username must be at most " + MAX_USERNAME_LENGTH + " characters";
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }
        
        return null;
    }
    
    /**
     * Validate password format.
     * @param password the password to validate
     * @return error message if invalid, null if valid
     */
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        
        return null;
    }
    
    /**
     * Validate email format.
     * @param email the email to validate
     * @return error message if invalid, null if valid
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        
        email = email.trim();
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format";
        }
        
        return null;
    }
    
    /**
     * Validates if a string is a valid email address format.
     * 
     * @param email the email string to validate
     * @return true if the email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Check if a string is null or empty.
     * @param str the string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
