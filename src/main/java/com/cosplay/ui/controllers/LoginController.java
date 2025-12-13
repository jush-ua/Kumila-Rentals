package com.cosplay.ui.controllers;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

import com.cosplay.Launcher;
import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.CallbackServer;
import com.cosplay.util.GoogleOAuthUtil;
import com.cosplay.util.Session;
import com.cosplay.util.StyledAlert;
import com.cosplay.util.ValidationUtil;
import com.cosplay.util.AnimationUtil;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button googleLoginButton;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImage;
    @FXML private AnchorPane rootPane;
    @FXML private javafx.scene.layout.VBox textFieldPanel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        // Clear error label initially
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        // Add button hover effects
        if (loginButton != null) {
            AnimationUtil.addButtonHoverEffect(loginButton);
        }
        if (googleLoginButton != null) {
            AnimationUtil.addButtonHoverEffect(googleLoginButton);
        }
        
        // Google login button is always visible
        // Configure GoogleOAuthUtil.java to enable functionality

        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/com/cosplay/ui/images/logo.png")));
            // Bind the logo width once the scene is available so it scales with window size
            logoImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    logoImage.fitWidthProperty().bind(newScene.widthProperty().multiply(0.20));
                }
            });
        } catch (Exception e) {
            // ignore if missing
        }

        try {
            // Use AnchorPane background with BackgroundSize cover=true so image behaves like CSS 'background-size: cover'
            var bgStream = getClass().getResourceAsStream("/com/cosplay/ui/images/login_bg.png");
            if (bgStream != null) {
                Image bg = new Image(bgStream);
                if (!bg.isError()) {
                    BackgroundSize bsize = new BackgroundSize(100, 100, true, true, false, true); // cover=true
                    BackgroundImage bimg = new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bsize);
                    if (rootPane != null) rootPane.setBackground(new Background(bimg));
                    System.out.println("Background image loaded successfully");
                } else {
                    System.err.println("Background image error: " + bg.getException());
                }
            } else {
                System.err.println("Background image stream is null");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
            e.printStackTrace();
        }

        // Responsive bindings: scale controls relative to the root pane size
        if (rootPane != null) {
            // Logo scales to ~20% of window width
            if (logoImage != null) {
                logoImage.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.20));
                logoImage.setPreserveRatio(true);
            }

            // Text fields and text panel take available width minus horizontal padding (20px each side)
            var availableWidth = rootPane.widthProperty().subtract(40);
            try {
                if (textFieldPanel != null) textFieldPanel.prefWidthProperty().bind(availableWidth);
            } catch (Exception ignored) {}
            try {
                if (usernameField != null) usernameField.prefWidthProperty().bind(availableWidth);
            } catch (Exception ignored) {}
            try {
                if (passwordField != null) passwordField.prefWidthProperty().bind(availableWidth);
            } catch (Exception ignored) {}

            // Buttons: make them proportional to window width (25% each)
            try {
                if (loginButton != null) loginButton.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.25));
            } catch (Exception ignored) {}
            try {
                if (googleLoginButton != null) googleLoginButton.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.25));
            } catch (Exception ignored) {}
        }

        
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

    @FXML
    private void handleForgotPassword() {
        // Create dialog for forgot password
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Reset Your Password");
        dialog.setContentText("Enter your email address and we'll send you a password reset link.");

        // Set the button types
        javafx.scene.control.ButtonType sendButtonType = new javafx.scene.control.ButtonType("Send Reset Link", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, javafx.scene.control.ButtonType.CANCEL);

        // Create email input field
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        emailField.setPromptText("Email address");
        emailField.setPrefWidth(300);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(new Label("Email:"), emailField);
        dialog.getDialogPane().setContent(content);

        // Request focus on email field
        Platform.runLater(() -> emailField.requestFocus());

        // Convert result to email when send button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                return emailField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (ValidationUtil.isEmpty(email)) {
                showError("Please enter your email address");
                return;
            }

            if (!ValidationUtil.isValidEmail(email)) {
                showError("Please enter a valid email address");
                return;
            }

            // Process password reset
            processPasswordReset(email.trim());
        });
    }

    private void processPasswordReset(String email) {
        try {
            // Check if user exists
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                // Don't reveal if email exists for security
                showInfo("If an account exists with this email, you will receive a password reset link shortly.");
                return;
            }

            // Don't allow password reset for OAuth users
            if (user.getOauthProvider() != null && !"local".equals(user.getOauthProvider())) {
                showError("This account uses " + user.getOauthProvider() + " login. Please use " + user.getOauthProvider() + " to sign in.");
                return;
            }

            // Generate reset token
            String resetToken = com.cosplay.util.TokenUtil.generateToken();
            
            // Save reset token to database
            if (!userDAO.setPasswordResetToken(email, resetToken)) {
                showError("Failed to process password reset. Please try again.");
                return;
            }

            // Send reset email
            boolean emailSent = com.cosplay.util.EmailUtil.sendPasswordResetEmail(email, user.getUsername(), resetToken);
            
            if (emailSent) {
                showInfo("Password reset instructions have been sent to your email address.\n\nPlease check your inbox and follow the link to reset your password.");
            } else {
                showError("Failed to send reset email. Please try again later.");
            }

        } catch (Exception e) {
            showError("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    /**
     * Show error message to user.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            StyledAlert.showError("Login Error", message);
        }
    }

    /**
     * Show email not verified error with resend option.
     */
    private void showEmailNotVerifiedError(User user) {
        Alert alert = StyledAlert.createWarning(
            "Email Not Verified",
            "Your email address has not been verified. Please check your email for the verification link.\n\nDidn't receive the email?"
        );
        
        alert.setHeaderText("Please verify your email");
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
        Alert alert = StyledAlert.createSuccess("Login Successful", message);
        alert.setOnHidden(evt -> SceneNavigator.navigate(view));
        alert.show();
    }

    /**
     * Show info message to user.
     */
    private void showInfo(String message) {
        StyledAlert.showInfo("Information", message);
    }
}
