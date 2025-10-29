package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import javafx.fxml.FXML;

public class CatalogController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.CATALOG);
        }
    }

    @FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
}
