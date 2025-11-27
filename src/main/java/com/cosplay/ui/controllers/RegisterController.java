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

public class RegisterController {
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private PasswordField confirmPasswordField;
	@FXML private TextField emailField;
	@FXML private Button registerButton;
	@FXML private Label errorLabel;

	private final UserDAO userDAO = new UserDAO();

	@FXML
	private void initialize() {
		// Clear error label initially
		if (errorLabel != null) {
			errorLabel.setText("");
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
