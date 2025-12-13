# Google OAuth Setup Guide

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Sign in with your Google account
3. Click **"Select a project"** dropdown at the top
4. Click **"NEW PROJECT"**
5. Enter project name: `Kumila-Rentals` (or your preferred name)
6. Click **"CREATE"**
7. Wait for project creation, then select it

## Step 2: Enable Google+ API

1. In the left sidebar, go to **"APIs & Services"** → **"Library"**
2. Search for **"Google+ API"** or **"Google People API"**
3. Click on it
4. Click **"ENABLE"**
5. Wait for it to be enabled

## Step 3: Configure OAuth Consent Screen

1. Go to **"APIs & Services"** → **"OAuth consent screen"**
2. Choose **"External"** (for testing with any Google account)
3. Click **"CREATE"**

4. Fill in the required fields:
   - **App name**: `Kumila Rentals`
   - **User support email**: Your email (e.g., `noreplyepou@gmail.com`)
   - **App logo**: (optional - skip for now)
   - **Application home page**: `http://localhost:8080` (for testing)
   - **Authorized domains**: (skip for localhost testing)
   - **Developer contact information**: Your email

5. Click **"SAVE AND CONTINUE"**

6. **Scopes page**:
   - Click **"ADD OR REMOVE SCOPES"**
   - Select these scopes:
     - `.../auth/userinfo.email`
     - `.../auth/userinfo.profile`
     - `openid`
   - Click **"UPDATE"**
   - Click **"SAVE AND CONTINUE"**

7. **Test users** (if using External):
   - Click **"ADD USERS"**
   - Add your test Google accounts (e.g., your personal Gmail)
   - Click **"ADD"**
   - Click **"SAVE AND CONTINUE"**

8. Review and click **"BACK TO DASHBOARD"**

## Step 4: Create OAuth Credentials

1. Go to **"APIs & Services"** → **"Credentials"**
2. Click **"+ CREATE CREDENTIALS"** at the top
3. Select **"OAuth client ID"**

4. Configure:
   - **Application type**: Select **"Desktop app"**
   - **Name**: `Kumila Rentals Desktop Client`
   - Click **"CREATE"**

5. **IMPORTANT**: In the credentials configuration:
   - Scroll down to **"Authorized redirect URIs"**
   - Click **"+ ADD URI"**
   - Enter: `http://localhost:8080/oauth2callback`
   - Click **"SAVE"**

6. A dialog will show your credentials:
   - **Client ID**: Something like `123456789-abc123.apps.googleusercontent.com`
   - **Client Secret**: Something like `GOCSPX-abc123def456`
   - Click **"DOWNLOAD JSON"** (optional - for backup)
   - **COPY THESE VALUES** - you'll need them next!

## Step 5: Update Your Application

1. Open `src/main/java/com/cosplay/util/GoogleOAuthUtil.java`

2. Find these lines (around line 15-16):
   ```java
   private static final String CLIENT_ID = "your-client-id.apps.googleusercontent.com";
   private static final String CLIENT_SECRET = "your-client-secret";
   ```

3. Replace with your actual credentials:
   ```java
   private static final String CLIENT_ID = "123456789-abc123.apps.googleusercontent.com"; // Your actual Client ID
   private static final String CLIENT_SECRET = "GOCSPX-abc123def456"; // Your actual Client Secret
   ```

4. **Save the file**

## Step 6: Test Google Login

1. **Run your application**:
   ```powershell
   mvn clean javafx:run
   ```

2. **Click "Login with Google"** button

3. **Browser will open** asking you to:
   - Choose your Google account
   - Review permissions (email, profile)
   - Click **"Allow"**

4. **You'll see a success page** in the browser

5. **Return to the app** - you should be logged in automatically!

## Troubleshooting

### "Google login is not configured"
- Make sure you replaced the CLIENT_ID and CLIENT_SECRET correctly
- Check for typos in the credentials
- Rebuild the project: `mvn clean compile`

### "Redirect URI mismatch" error
- In Google Cloud Console, verify the redirect URI is exactly: `http://localhost:8080/oauth2callback`
- No trailing slash, must match exactly
- May need to wait a few minutes after saving changes

### "Access blocked: This app's request is invalid"
- Make sure you enabled the Google+ API or Google People API
- Check that OAuth consent screen is configured
- Add your test email to "Test users" list

### "Sign in with Google temporarily disabled"
- OAuth consent screen needs to be published or in testing mode
- Add yourself as a test user

### Browser doesn't open
- Check if Desktop is supported: the app will show an error
- Manually copy the URL from error message

### Callback server not receiving code
- Check console for "Callback server started successfully"
- Verify port 8080 is not blocked by firewall
- Make sure another application isn't using port 8080

## Security Notes

### For Development
- Current setup uses `http://localhost:8080` (HTTP)
- Only works on your local machine
- Perfect for testing

### For Production
You would need to:
1. Get a domain name
2. Set up HTTPS (SSL certificate)
3. Update redirect URI to `https://yourdomain.com/oauth2callback`
4. Update `APP_URL` in `EmailUtil.java`
5. Publish the OAuth consent screen
6. Set application verification (for public apps)

## Important Security Reminders

🔒 **Never commit your credentials to Git!**
   - Add `GoogleOAuthUtil.java` to `.gitignore` if sharing code
   - Or use environment variables instead of hardcoded values

🔐 **Keep your Client Secret private**
   - Don't share in public repositories
   - Don't include in screenshots
   - Treat it like a password

## Quick Reference

**Google Cloud Console**: https://console.cloud.google.com/
**Required Redirect URI**: `http://localhost:8080/oauth2callback`
**Required Scopes**: `openid`, `email`, `profile`
**File to Edit**: `src/main/java/com/cosplay/util/GoogleOAuthUtil.java`

---

**Need help?** Check the [Google OAuth Documentation](https://developers.google.com/identity/protocols/oauth2)
