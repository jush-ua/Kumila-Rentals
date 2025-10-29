package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import javafx.fxml.FXML;

public class AboutController {
    // Included NavBar controller
    @FXML private NavController navBarController;

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.ABOUT);
        }
    }

    @FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
}
