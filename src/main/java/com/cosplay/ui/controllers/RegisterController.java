package com.cosplay.ui.controllers;

import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private TextField emailField;

	private UserDAO userDAO = new UserDAO();

	@FXML
	private void handleRegister() {
		String username = usernameField.getText();
		String password = passwordField.getText();
		String email = emailField.getText();

		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		u.setEmail(email);

		boolean ok = userDAO.createUser(u);
		Alert a = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
		a.setContentText(ok ? "Registration successful" : "Registration failed (maybe username taken)");
		a.show();
	}
}
