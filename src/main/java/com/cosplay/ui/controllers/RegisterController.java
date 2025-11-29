package com.cosplay.ui.controllers;

import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.EmailUtil;
import com.cosplay.util.TokenUtil;
import com.cosplay.util.ValidationUtil;
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
import javafx.scene.layout.VBox;

public class RegisterController {
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private PasswordField confirmPasswordField;
	@FXML private TextField emailField;
	@FXML private Button registerButton;
	@FXML private Label errorLabel;
	@FXML private ImageView logoImage;
	@FXML private AnchorPane rootPane;
	@FXML private VBox textFieldPanel;

	private final UserDAO userDAO = new UserDAO();

	@FXML
	private void initialize() {
		// Clear error label initially
		if (errorLabel != null) {
			errorLabel.setText("");
		}

		// Load logo and background similar to LoginView
		try {
			if (logoImage != null) {
				logoImage.setImage(new Image(getClass().getResourceAsStream("/com/cosplay/ui/images/logo.png")));
				logoImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
					if (newScene != null) {
						logoImage.fitWidthProperty().bind(newScene.widthProperty().multiply(0.20));
					}
				});
			}
		} catch (Exception ignored) {}

		try {
			Image bg = new Image(getClass().getResourceAsStream("/com/cosplay/ui/images/login_bg.png"));
			BackgroundSize bsize = new BackgroundSize(100, 100, true, true, false, true);
			BackgroundImage bimg = new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bsize);
			if (rootPane != null) rootPane.setBackground(new Background(bimg));
		} catch (Exception ignored) {}

		// Responsive bindings similar to LoginController
		if (rootPane != null) {
			var availableWidth = rootPane.widthProperty().subtract(40);
			try { if (textFieldPanel != null) textFieldPanel.prefWidthProperty().bind(availableWidth); } catch (Exception ignored) {}
			try { if (usernameField != null) usernameField.prefWidthProperty().bind(availableWidth); } catch (Exception ignored) {}
			try { if (emailField != null) emailField.prefWidthProperty().bind(availableWidth); } catch (Exception ignored) {}
			try { if (passwordField != null) passwordField.prefWidthProperty().bind(availableWidth); } catch (Exception ignored) {}
			try { if (registerButton != null) registerButton.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.25)); } catch (Exception ignored) {}
		}
	}

	@FXML
	private void handleRegister() {
		// Clear previous errors
		if (errorLabel != null) {
			errorLabel.setText("");
		}

		String username = usernameField.getText();
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : password;
		String email = emailField.getText();

		// Validate username
		String usernameError = ValidationUtil.validateUsername(username);
		if (usernameError != null) {
			showError(usernameError);
			return;
		}

		// Validate password
		String passwordError = ValidationUtil.validatePassword(password);
		if (passwordError != null) {
			showError(passwordError);
			return;
		}

		// Validate password confirmation
		if (confirmPasswordField != null && !password.equals(confirmPassword)) {
			showError("Passwords do not match");
			return;
		}

		// Validate email
		String emailError = ValidationUtil.validateEmail(email);
		if (emailError != null) {
			showError(emailError);
			return;
		}

		// Disable button to prevent multiple clicks
		if (registerButton != null) {
			registerButton.setDisable(true);
		}

		try {
			// Check if username already exists
			if (userDAO.usernameExists(username.trim())) {
				showError("Username already taken. Please choose a different username.");
				return;
			}

			// Check if email already exists
			if (userDAO.emailExists(email.trim())) {
				showError("Email already registered. Please use a different email.");
				return;
			}

			// Generate verification token
			String verificationToken = TokenUtil.generateToken();

			// Create new user
			User u = new User();
			u.setUsername(username.trim());
			u.setPassword(password); // Will be hashed in DAO
			u.setEmail(email.trim());
			u.setRole("customer");
			u.setEmailVerified(false); // Not verified yet
			u.setVerificationToken(verificationToken);
			u.setOauthProvider("local");

			boolean ok = userDAO.createUser(u);
			
			if (ok) {
				// Send verification email
				boolean emailSent = EmailUtil.sendVerificationEmail(email.trim(), username.trim(), verificationToken);
				
				if (emailSent) {
					showSuccessAndNavigate(
						"Registration successful! Please check your email (" + email.trim() + ") " +
						"to verify your account before logging in."
					);
				} else {
					showSuccessAndNavigate(
						"Registration successful! However, we couldn't send the verification email. " +
						"Please contact support or try to resend the verification email."
					);
				}
			} else {
				showError("Registration failed. Please try again.");
			}
		} catch (Exception e) {
			showError("An error occurred during registration. Please try again.");
			e.printStackTrace();
		} finally {
			// Re-enable button
			if (registerButton != null) {
				registerButton.setDisable(false);
			}
		}
	}

	@FXML
	private void goToLogin() {
		SceneNavigator.navigate(Views.LOGIN);
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
			alert.setTitle("Registration Error");
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		}
	}

	/**
	 * Show success message and navigate to login view.
	 */
	private void showSuccessAndNavigate(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Registration Successful");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.setOnHidden(evt -> SceneNavigator.navigate(Views.LOGIN));
		alert.show();
	}
}
