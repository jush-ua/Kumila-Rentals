package com.cosplay.ui.controllers;

import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = userDAO.login(username, password);
        Alert alert = new Alert(user != null ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setContentText(user != null ? "Welcome, " + user.getUsername() + "!" : "Invalid credentials");
        alert.show();
    }

    @FXML
    private void goToRegister() {
        System.out.println("Navigate to Register Page...");
    }
}
