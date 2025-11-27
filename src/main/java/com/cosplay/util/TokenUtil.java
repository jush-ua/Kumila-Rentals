package com.cosplay.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating secure tokens.
 */
public class TokenUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    
    /**
     * Generate a secure random token.
     * @param byteLength the length of the token in bytes (default 32)
     * @return a URL-safe base64 encoded token
     */
    public static String generateToken(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    
    /**
     * Generate a secure random token with default length (32 bytes).
     * @return a URL-safe base64 encoded token
     */
    public static String generateToken() {
        return generateToken(32);
    }
}
