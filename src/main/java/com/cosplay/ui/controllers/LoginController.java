package com.cosplay.ui.controllers;

import com.cosplay.dao.UserDAO;
import com.cosplay.model.User;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = userDAO.login(username, password);
        Alert alert = new Alert(user != null ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setContentText(user != null ? "Welcome, " + user.getUsername() + "!" : "Invalid credentials");
        alert.show();
        if (user != null) {
            Session.setCurrentUser(user);
            // Navigate to Home screen
            SceneNavigator.navigate(Views.HOME);
        }
    }

    @FXML
    private void goToRegister() {
        SceneNavigator.navigate(Views.REGISTER);
    }
}
