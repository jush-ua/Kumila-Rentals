package com.cosplay.util;

import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Simple HTTP server for handling OAuth callbacks and email verification.
 */
public class CallbackServer {
    private HttpServer server;
    private static final int PORT = 8080;
    private CompletableFuture<String> authCodeFuture;
    private Consumer<User> onOAuthSuccess;
    private final Set<String> codesBeingProcessed = new HashSet<>(); // Shared across all requests
    private String lastProcessedCode = null; // Track last processed code
    private final Object lock = new Object(); // Explicit lock object

    /**
     * Handler for email verification.
     */
    private class EmailVerificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String token = params.get("token");

            String response;
            int statusCode;

            if (token == null || token.isEmpty()) {
                response = generateErrorPage("Invalid Link", "Verification token is missing.");
                statusCode = 400;
            } else {
                UserDAO userDAO = new UserDAO();
                boolean verified = userDAO.verifyEmail(token);

                if (verified) {
                    response = generateSuccessPage("Email Verified!",
                        "Your email has been successfully verified. You can now close this window and log in to the application.");
                    statusCode = 200;
                } else {
                    response = generateErrorPage("Verification Failed",
                        "Invalid or expired verification token. Please request a new verification email.");
                    statusCode = 400;
                }
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
    
    /**
     * Start the callback server.
     */
    public void start() throws IOException {
        if (server != null) {
            return; // Already running
        }
        
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // OAuth callback endpoint
        server.createContext("/oauth2callback", new OAuthCallbackHandler());
        
        // Email verification endpoint
        server.createContext("/verify", new EmailVerificationHandler());
        
        server.setExecutor(null); // Use default executor
        server.start();
        
        System.out.println("Callback server started on http://localhost:" + PORT);
    }
    
    /**
     * Stop the callback server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Callback server stopped");
        }
    }
    
    /**
     * Wait for OAuth authorization code.
     * @return CompletableFuture that completes with the authorization code
     */
    public CompletableFuture<String> waitForAuthCode() {
        authCodeFuture = new CompletableFuture<>();
        return authCodeFuture;
    }
    
    /**
     * Set callback for successful OAuth authentication.
     * @param callback the callback to execute with the authenticated user
     */
    public void setOAuthSuccessCallback(Consumer<User> callback) {
        this.onOAuthSuccess = callback;
    }
    
    /**
     * Handler for OAuth callback.
     */
    private class OAuthCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String authCode = params.get("code");
            String error = params.get("error");
            
            String response = "";
            int statusCode = 500;
            
            if (error != null) {
                response = generateErrorPage("OAuth Error", "Authentication failed: " + error);
                statusCode = 400;
                if (authCodeFuture != null) {
                    authCodeFuture.completeExceptionally(new Exception("OAuth error: " + error));
                }
            } else if (authCode != null) {
                // Prevent duplicate processing of the same code
                boolean shouldProcess = false;
                String threadId = Thread.currentThread().getName();
                System.out.println("DEBUG [" + threadId + "] - Checking code: " + authCode.substring(0, Math.min(15, authCode.length())) + "...");
                
                synchronized (lock) {
                    System.out.println("DEBUG [" + threadId + "] - Inside synchronized block. Set size: " + codesBeingProcessed.size());
                    System.out.println("DEBUG [" + threadId + "] - Set contents: " + codesBeingProcessed);
                    if (codesBeingProcessed.contains(authCode)) {
                        System.out.println("DEBUG [" + threadId + "] - Code already in processing set - rejecting");
                        response = generateSuccessPage("Processing", "Your login is being processed. Please wait...");
                        statusCode = 200;
                    } else if (authCode.equals(lastProcessedCode)) {
                        System.out.println("DEBUG [" + threadId + "] - Code already processed - rejecting");
                        response = generateSuccessPage("Already Processed", "Your login has been processed. You can close this window.");
                        statusCode = 200;
                    } else {
                        System.out.println("DEBUG [" + threadId + "] - Adding code to processing set");
                        codesBeingProcessed.add(authCode);
                        shouldProcess = true;
                        System.out.println("DEBUG [" + threadId + "] - Set size after add: " + codesBeingProcessed.size());
                    }
                }
                
                if (shouldProcess) {
                    // Try to exchange code for user info
                    System.out.println("DEBUG - Received authorization code: " + authCode.substring(0, Math.min(20, authCode.length())) + "...");
                    System.out.println("DEBUG - Starting OAuth token exchange...");
                    try {
                        GoogleOAuthUtil.GoogleUserInfo userInfo = GoogleOAuthUtil.getUserInfo(authCode);
                        System.out.println("DEBUG - OAuth token exchange completed. UserInfo result: " + (userInfo != null ? "SUCCESS" : "NULL"));
                        
                        if (userInfo != null) {
                            System.out.println("DEBUG - Successfully got user info: " + userInfo.getEmail());
                            // Use Google account name, fallback to email prefix if name not available
                            String username = userInfo.getName() != null && !userInfo.getName().isEmpty() 
                                ? userInfo.getName().replaceAll("[^a-zA-Z0-9_]", "") // Remove special chars
                                : userInfo.getEmail().split("@")[0];
                            UserDAO userDAO = new UserDAO();
                        
                            // Check if username exists, add number if needed
                            String finalUsername = username;
                            int counter = 1;
                            while (userDAO.usernameExists(finalUsername)) {
                                finalUsername = username + counter;
                                counter++;
                            }
                            
                            // Find or create user
                            System.out.println("DEBUG - Attempting to find/create OAuth user...");
                            User user = userDAO.findOrCreateOAuthUser(
                                "google",
                                userInfo.getId(),
                                userInfo.getEmail(),
                                finalUsername
                            );
                            
                            if (user != null) {
                                System.out.println("DEBUG - User created/found successfully: " + user.getUsername());
                                System.out.println("DEBUG - User ID: " + user.getUserId());
                                
                                // Mark code as successfully processed
                                synchronized (lock) {
                                    lastProcessedCode = authCode;
                                    codesBeingProcessed.remove(authCode);
                                }
                                
                                response = generateSuccessPage("Login Successful!", 
                                    "Welcome, " + user.getUsername() + "! You can close this window and return to the application.");
                                statusCode = 200;
                                
                                // Notify callback
                                System.out.println("DEBUG - Calling onOAuthSuccess callback...");
                                if (onOAuthSuccess != null) {
                                    onOAuthSuccess.accept(user);
                                    System.out.println("DEBUG - Callback executed successfully");
                                } else {
                                    System.err.println("DEBUG - WARNING: onOAuthSuccess callback is NULL!");
                                }
                                
                                if (authCodeFuture != null) {
                                    authCodeFuture.complete(authCode);
                                }
                            } else {
                                System.err.println("DEBUG - ERROR: Failed to create user account (user is null)");
                                response = generateErrorPage("Error", "Failed to create user account.");
                                statusCode = 500;
                            }
                        } else {
                            System.err.println("DEBUG - ERROR: Failed to get user information from Google.");
                            response = generateErrorPage("Error", "Failed to get user information from Google.");
                            statusCode = 500;
                            // Don't remove from set on error - let it stay to block duplicates
                        }
                    } catch (Exception e) {
                        System.err.println("DEBUG - Exception during OAuth: " + e.getClass().getName());
                        System.err.println("DEBUG - Exception message: " + e.getMessage());
                        e.printStackTrace();
                        response = generateErrorPage("Error", "Authentication failed: " + e.getMessage());
                        statusCode = 500;
                        // Don't remove from set on error - let it stay to block duplicates
                    }
                }
            } else {
                response = generateErrorPage("Error", "No authorization code received.");
                statusCode = 400;
            }
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
    
    /**
     * Parse query string into map.
     */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
    
    /**
     * Generate success HTML page.
     */
    private String generateSuccessPage(String title, String message) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><title>" + title + "</title>" +
               "<style>body{font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;" +
               "height:100vh;margin:0;background:#f0f0f0;}.container{background:white;padding:40px;border-radius:8px;" +
               "box-shadow:0 2px 10px rgba(0,0,0,0.1);text-align:center;max-width:500px;}.success{color:#4CAF50;font-size:48px;}" +
               "h1{color:#333;margin:20px 0;}p{color:#666;line-height:1.6;}</style></head>" +
               "<body><div class='container'><div class='success'>✓</div><h1>" + title + "</h1>" +
               "<p>" + message + "</p></div></body></html>";
    }
    
    /**
     * Generate error HTML page.
     */
    private String generateErrorPage(String title, String message) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><title>" + title + "</title>" +
               "<style>body{font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;" +
               "height:100vh;margin:0;background:#f0f0f0;}.container{background:white;padding:40px;border-radius:8px;" +
               "box-shadow:0 2px 10px rgba(0,0,0,0.1);text-align:center;max-width:500px;}.error{color:#f44336;font-size:48px;}" +
               "h1{color:#333;margin:20px 0;}p{color:#666;line-height:1.6;}</style></head>" +
               "<body><div class='container'><div class='error'>✗</div><h1>" + title + "</h1>" +
               "<p>" + message + "</p></div></body></html>";
    }
}
