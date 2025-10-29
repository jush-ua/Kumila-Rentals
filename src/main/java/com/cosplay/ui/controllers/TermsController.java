package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import javafx.fxml.FXML;

public class TermsController {
    // Included NavBar controller
    @FXML private NavController navBarController;

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.TERMS);
        }
    }

    @FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
}
