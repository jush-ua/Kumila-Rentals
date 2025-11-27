package com.cosplay.ui.controllers;

import com.cosplay.Launcher;
import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.CallbackServer;
import com.cosplay.util.GoogleOAuthUtil;
import com.cosplay.util.Session;
import com.cosplay.util.ValidationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button googleLoginButton;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        // Clear error label initially
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        // Google login button is always visible
        // Configure GoogleOAuthUtil.java to enable functionality
    }

    @FXML
    private void handleLogin() {
        // Clear previous errors
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate inputs
        if (ValidationUtil.isEmpty(username)) {
            showError("Please enter your username");
            return;
        }

        if (ValidationUtil.isEmpty(password)) {
            showError("Please enter your password");
            return;
        }

        // Disable button to prevent multiple clicks
        if (loginButton != null) {
            loginButton.setDisable(true);
        }

        try {
            // Attempt login
            User user = userDAO.login(username.trim(), password);
            
            if (user != null) {
                // Check if email is verified (only for local accounts)
                if ("local".equals(user.getOauthProvider()) && !user.isEmailVerified()) {
                    showEmailNotVerifiedError(user);
                    return;
                }
                
                // Login successful
                Session.setCurrentUser(user);
                showSuccessAndNavigate("Welcome, " + user.getUsername() + "!", Views.HOME);
            } else {
                // Login failed
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            showError("An error occurred during login. Please try again.");
            e.printStackTrace();
        } finally {
            // Re-enable button
            if (loginButton != null) {
                loginButton.setDisable(false);
            }
        }
    }

    @FXML
    private void handleGoogleLogin() {
        if (!GoogleOAuthUtil.isConfigured()) {
            showError("Google login is not configured. Please set up OAuth credentials.");
            return;
        }

        try {
            // Get the callback server from Launcher
            CallbackServer callbackServer = Launcher.getCallbackServer();
            
            if (callbackServer == null) {
                showError("Callback server is not running. Please restart the application.");
                return;
            }

            // Set up OAuth success callback
            callbackServer.setOAuthSuccessCallback(user -> {
                Platform.runLater(() -> {
                    Session.setCurrentUser(user);
                    showSuccessAndNavigate("Welcome, " + user.getUsername() + "!", Views.HOME);
                });
            });

            // Get Google authorization URL
            String authUrl = GoogleOAuthUtil.getAuthorizationUrl();
            if (authUrl == null) {
                showError("Failed to generate Google login URL.");
                return;
            }

            // Show waiting message
            showInfo("Opening browser for Google login...\nPlease authorize the application in your browser.");

            // Open browser for user to authenticate
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                showError("Could not open browser. Please visit: " + authUrl);
            }

        } catch (Exception e) {
            showError("Error during Google login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        SceneNavigator.navigate(Views.REGISTER);
    }

    /**
     * Show error message to user.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    /**
     * Show email not verified error with resend option.
     */
    private void showEmailNotVerifiedError(User user) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Email Not Verified");
        alert.setHeaderText("Please verify your email");
        alert.setContentText("Your email address has not been verified. Please check your email for the verification link.\n\nDidn't receive the email?");
        
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK,
            new javafx.scene.control.ButtonType("Resend Email")
        );

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().getText().equals("Resend Email")) {
            // TODO: Implement resend verification email
            showInfo("Verification email resent. Please check your inbox.");
        }
    }

    /**
     * Show success message and navigate to another view.
     */
    private void showSuccessAndNavigate(String message, Views view) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setOnHidden(evt -> SceneNavigator.navigate(view));
        alert.show();
    }

    /**
     * Show info message to user.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
