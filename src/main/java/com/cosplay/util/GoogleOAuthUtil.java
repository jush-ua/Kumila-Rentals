package com.cosplay.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for Google OAuth authentication.
 */
public class GoogleOAuthUtil {
    // GOOGLE OAUTH CREDENTIALS - Set these as environment variables
    // Get these from: https://console.cloud.google.com/
    // Set environment variables: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET
    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://localhost:8080/oauth2callback";
    
    private static final List<String> SCOPES = Arrays.asList(
        "openid",
        "email",
        "profile"
    );
    
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    /**
     * Get the Google authorization URL for the user to visit.
     * @return the authorization URL
     */
    public static String getAuthorizationUrl() {
        try {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(CLIENT_ID);
            details.setClientSecret(CLIENT_SECRET);
            
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            clientSecrets.setWeb(details);
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, 
                JSON_FACTORY, 
                clientSecrets, 
                SCOPES
            )
            .setAccessType("online")
            .build();
            
            return flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .build();
        } catch (Exception e) {
            System.err.println("Error generating authorization URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Exchange authorization code for user information.
     * @param authorizationCode the authorization code from Google
     * @return GoogleUserInfo object with user details, or null if failed
     */
    public static GoogleUserInfo getUserInfo(String authorizationCode) {
        try {
            System.out.println("DEBUG getUserInfo - Step 1: Creating client secrets");
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(CLIENT_ID);
            details.setClientSecret(CLIENT_SECRET);
            
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            clientSecrets.setWeb(details);
            
            System.out.println("DEBUG getUserInfo - Step 2: Building authorization flow");
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, 
                JSON_FACTORY, 
                clientSecrets, 
                SCOPES
            )
            .setAccessType("online")
            .build();
            
            // Exchange code for token
            System.out.println("DEBUG getUserInfo - Step 3: Exchanging authorization code for token");
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(REDIRECT_URI)
                .execute();
            System.out.println("DEBUG getUserInfo - Step 4: Token received successfully");
            System.out.println("DEBUG getUserInfo - Access token: " + tokenResponse.getAccessToken().substring(0, 20) + "...");
            
            // Get user info using direct HTTP request (more reliable than OAuth2 library)
            System.out.println("DEBUG getUserInfo - Step 5: Fetching user info via HTTP");
            String accessToken = tokenResponse.getAccessToken();
            URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setConnectTimeout(10000); // 10 second timeout
            conn.setReadTimeout(10000);
            
            System.out.println("DEBUG getUserInfo - Step 6: Connecting to Google API");
            int responseCode = conn.getResponseCode();
            System.out.println("DEBUG getUserInfo - Step 7: Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                System.out.println("DEBUG getUserInfo - Step 8: Parsing user info JSON");
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                System.out.println("DEBUG getUserInfo - Step 9: User info received successfully");
                
                return new GoogleUserInfo(
                    jsonObject.get("id").getAsString(),
                    jsonObject.get("email").getAsString(),
                    jsonObject.has("name") ? jsonObject.get("name").getAsString() : null,
                    jsonObject.has("given_name") ? jsonObject.get("given_name").getAsString() : null,
                    jsonObject.has("family_name") ? jsonObject.get("family_name").getAsString() : null,
                    jsonObject.has("picture") ? jsonObject.get("picture").getAsString() : null,
                    jsonObject.has("verified_email") ? jsonObject.get("verified_email").getAsBoolean() : false
                );
            } else {
                System.err.println("DEBUG getUserInfo - ERROR: Failed to get user info. Response code: " + responseCode);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error getting user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if Google OAuth is properly configured.
     * @return true if configured, false otherwise
     */
    public static boolean isConfigured() {
        System.out.println("DEBUG - CLIENT_ID: " + CLIENT_ID);
        System.out.println("DEBUG - CLIENT_SECRET: " + CLIENT_SECRET);
        boolean configured = !CLIENT_ID.equals("your-client-id.apps.googleusercontent.com") && 
                            !CLIENT_SECRET.equals("your-client-secret");
        System.out.println("DEBUG - isConfigured result: " + configured);
        return configured;
    }
    
    /**
     * Container class for Google user information.
     */
    public static class GoogleUserInfo {
        private final String id;
        private final String email;
        private final String name;
        private final String givenName;
        private final String familyName;
        private final String picture;
        private final Boolean emailVerified;
        
        public GoogleUserInfo(String id, String email, String name, String givenName, 
                            String familyName, String picture, Boolean emailVerified) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.givenName = givenName;
            this.familyName = familyName;
            this.picture = picture;
            this.emailVerified = emailVerified;
        }
        
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
        public String getPicture() { return picture; }
        public Boolean getEmailVerified() { return emailVerified; }
    }
}
