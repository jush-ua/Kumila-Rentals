# Email Verification & Google Login Setup Guide

This guide explains how to configure email verification and Google OAuth login for the Kumila Rentals application.

## Features Added

### 1. Email Verification
- Users must verify their email after registration
- Verification token sent via email
- Users cannot log in until email is verified
- Resend verification email option

### 2. Google OAuth Login
- Sign in with Google account
- Automatic user creation for new Google users
- Pre-verified email (Google accounts are already verified)
- No password required for OAuth users

## Configuration Required

### Email Service Configuration

#### Option 1: Gmail SMTP (Recommended for Development)

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate an App Password**:
   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and your device
   - Copy the generated 16-character password

3. **Update `EmailUtil.java`**:
```java
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String EMAIL_USERNAME = "your-actual-email@gmail.com";
private static final String EMAIL_PASSWORD = "your-16-char-app-password";
private static final String FROM_EMAIL = "your-actual-email@gmail.com";
```

#### Option 2: Other Email Providers

**Outlook/Hotmail:**
```java
private static final String SMTP_HOST = "smtp.office365.com";
private static final String SMTP_PORT = "587";
```

**Yahoo Mail:**
```java
private static final String SMTP_HOST = "smtp.mail.yahoo.com";
private static final String SMTP_PORT = "587";
```

### Google OAuth Configuration

1. **Create Google Cloud Project**:
   - Go to: https://console.cloud.google.com/
   - Create a new project or select existing
   - Enable Google+ API

2. **Create OAuth 2.0 Credentials**:
   - Navigate to: APIs & Services > Credentials
   - Click "Create Credentials" > "OAuth client ID"
   - Application type: "Desktop app"
   - Name: "Kumila Rentals Desktop"
   - Click "Create"

3. **Configure Authorized Redirect URIs**:
   - Add: `http://localhost:8080/oauth2callback`
   - For production, add your actual domain

4. **Update `GoogleOAuthUtil.java`**:
```java
private static final String CLIENT_ID = "your-client-id.apps.googleusercontent.com";
private static final String CLIENT_SECRET = "your-client-secret";
private static final String REDIRECT_URI = "http://localhost:8080/oauth2callback";
```

## Database Schema Updates

The database schema has been updated with new columns:

```sql
-- New columns in users table
ALTER TABLE users ADD COLUMN email_verified INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(20);
ALTER TABLE users ADD COLUMN oauth_id VARCHAR(255);
```

These changes are applied automatically when you run the application.

## How It Works

### Email Verification Flow

1. **User Registration**:
   - User fills registration form
   - System generates unique verification token
   - Token stored in database with user record
   - Verification email sent to user's email address

2. **Email Verification**:
   - User clicks link in email (contains token)
   - System validates token
   - Marks email as verified in database
   - User can now log in

3. **Login Check**:
   - System checks if email is verified
   - Local accounts must have verified email
   - OAuth accounts are pre-verified

### Google Login Flow

1. **User Clicks "Login with Google"**:
   - System generates Google authorization URL
   - Opens user's browser to Google consent screen

2. **User Authorizes**:
   - User logs into Google account
   - Grants permissions to application
   - Google redirects back with authorization code

3. **User Enters Code**:
   - User pastes authorization code in dialog
   - System exchanges code for user information
   - System creates or finds existing user account

4. **Automatic Login**:
   - User automatically logged in
   - Session created with user details

## Testing Without Configuration

### Email (Development Mode)

If email is not configured, the system will:
- Print verification emails to console instead of sending
- Still create verification tokens
- Allow testing without real email service

Example console output:
```
=== EMAIL NOT CONFIGURED ===
To: user@example.com
Subject: Verify Your Email - Kumila Rentals
Body: Hello username, ...
============================
```

### Google OAuth (Development Mode)

If Google OAuth is not configured:
- Google login button will be hidden
- Users can still register and log in normally
- No impact on core functionality

## User Model Updates

The `User` model now includes:

```java
private boolean emailVerified;      // Email verification status
private String verificationToken;   // Token for email verification
private String oauthProvider;       // "local", "google", etc.
private String oauthId;            // OAuth provider's user ID
```

## Security Considerations

### Email Verification
- **Token Security**: Tokens are cryptographically secure (32 bytes, URL-safe Base64)
- **Token Expiry**: Consider implementing token expiration (e.g., 24 hours)
- **One-Time Use**: Tokens should be cleared after successful verification

### Google OAuth
- **Client Secret**: Keep CLIENT_SECRET confidential
- **HTTPS**: Use HTTPS in production
- **Redirect URI Validation**: Google validates redirect URIs
- **Scope Limitation**: Only requests necessary scopes (email, profile)

### Password Security
- **OAuth Users**: OAuth users don't need passwords
- **Password Field**: NULL for OAuth users in database
- **Hybrid Accounts**: Users can't mix OAuth and password login

## API Endpoints Needed (Future)

For full functionality, you'll need to implement:

1. **Email Verification Endpoint**:
   - `GET /verify?token=xxx`
   - Validates token and marks email as verified

2. **OAuth Callback Endpoint**:
   - `GET /oauth2callback?code=xxx`
   - Handles OAuth redirect and exchanges code

3. **Resend Verification Email**:
   - `POST /resend-verification`
   - Generates new token and resends email

## Troubleshooting

### Email Not Sending

**Check:**
- SMTP credentials are correct
- App password (not regular password) for Gmail
- Port 587 is not blocked by firewall
- Less secure app access enabled (if not using app password)

**Common Errors:**
```
AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```
**Solution**: Use app password, not regular password

### Google Login Not Working

**Check:**
- OAuth credentials are correct
- Redirect URI matches exactly
- Google+ API is enabled
- Desktop application type is selected

**Common Errors:**
```
redirect_uri_mismatch
```
**Solution**: Verify redirect URI in Google Console matches code

### Email Verification Loop

**Issue**: User can't verify email

**Check:**
- Token is correctly stored in database
- Token in email link matches database
- Email verification endpoint is working
- Token hasn't been used already

## Production Deployment

### Email Service
1. Use dedicated email service (SendGrid, Mailgun, AWS SES)
2. Configure SPF, DKIM, DMARC records
3. Monitor email deliverability
4. Implement rate limiting

### Google OAuth
1. Update redirect URIs to production domain
2. Use environment variables for credentials
3. Enable additional security features
4. Monitor OAuth usage in Google Console

### Database
1. Add indexes on frequently queried columns:
   ```sql
   CREATE INDEX idx_verification_token ON users(verification_token);
   CREATE INDEX idx_oauth_provider_id ON users(oauth_provider, oauth_id);
   CREATE INDEX idx_email ON users(email);
   ```

2. Consider token expiration:
   ```sql
   ALTER TABLE users ADD COLUMN token_expires_at TIMESTAMP;
   ```

## Files Modified/Created

### New Files
- `EmailUtil.java` - Email sending functionality
- `TokenUtil.java` - Secure token generation
- `GoogleOAuthUtil.java` - Google OAuth integration
- `OAUTH_SETUP_GUIDE.md` - This configuration guide

### Modified Files
- `User.java` - Added email verification and OAuth fields
- `Database.java` - Added new columns to schema
- `UserDAO.java` - Email verification and OAuth methods
- `LoginController.java` - Email verification check, Google login
- `RegisterController.java` - Email verification token generation
- `LoginView.fxml` - Google login button
- `app.css` - Google button styling
- `pom.xml` - Added JavaMail and Google OAuth dependencies

## Support

For issues or questions:
1. Check the console logs for error details
2. Verify configuration settings
3. Test email/OAuth services independently
4. Review Google Cloud Console logs

## Next Steps

1. Configure email service (see above)
2. Configure Google OAuth (see above)
3. Test email verification flow
4. Test Google login flow
5. Implement web endpoints for verification
6. Add token expiration logic
7. Implement password reset functionality
8. Add more OAuth providers (Facebook, Microsoft, etc.)
