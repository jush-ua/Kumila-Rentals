package com.cosplay.util;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Utility class for sending emails.
 * Configure your SMTP settings before use.
 */
public class EmailUtil {
    // Email configuration - REPLACE WITH YOUR ACTUAL SMTP SETTINGS
    private static final String SMTP_HOST = "smtp.gmail.com"; // For Gmail
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "noreplyepou@gmail.com"; // Your email
    private static final String EMAIL_PASSWORD = "stxm nepb hbvb gdcr"; // Your app password
    private static final String FROM_EMAIL = "noreplyepou@gmail.com";
    private static final String FROM_NAME = "Kumila Rentals";
    
    // Application URL for verification links
    private static final String APP_URL = "http://localhost:8080"; // Update for production
    
    /**
     * Send verification email to a user.
     * @param toEmail recipient email address
     * @param username recipient username
     * @param verificationToken the verification token
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendVerificationEmail(String toEmail, String username, String verificationToken) {
        String subject = "Verify Your Email - Kumila Rentals";
        String verificationLink = APP_URL + "/verify?token=" + verificationToken;
        
        String body = "Hello " + username + ",\n\n" +
                     "Thank you for registering with Kumila Rentals!\n\n" +
                     "Please verify your email address by clicking the link below:\n" +
                     verificationLink + "\n\n" +
                     "This link will expire in 24 hours.\n\n" +
                     "If you didn't create this account, please ignore this email.\n\n" +
                     "Best regards,\n" +
                     "Kumila Rentals Team";
        
        return sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send a password reset email.
     * @param toEmail recipient email address
     * @param username recipient username
     * @param resetToken the password reset token
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String subject = "Password Reset Request - Kumila Rentals";
        String resetLink = APP_URL + "/reset-password?token=" + resetToken;
        
        String body = "Hello " + username + ",\n\n" +
                     "We received a request to reset your password.\n\n" +
                     "Click the link below to reset your password:\n" +
                     resetLink + "\n\n" +
                     "This link will expire in 1 hour.\n\n" +
                     "If you didn't request this, please ignore this email.\n\n" +
                     "Best regards,\n" +
                     "Kumila Rentals Team";
        
        return sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send a generic email.
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body
     * @return true if email sent successfully, false otherwise
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        // Check if email is configured
        if (EMAIL_USERNAME.equals("your-email@gmail.com") || EMAIL_PASSWORD.equals("your-app-password")) {
            System.out.println("=== EMAIL NOT CONFIGURED ===");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("============================");
            // Return true for development/testing without actual email
            return true;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if email service is properly configured.
     * @return true if configured, false otherwise
     */
    public static boolean isConfigured() {
        return !EMAIL_USERNAME.equals("your-email@gmail.com") && 
               !EMAIL_PASSWORD.equals("your-app-password");
    }
}
