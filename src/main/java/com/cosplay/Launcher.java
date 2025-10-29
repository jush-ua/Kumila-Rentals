package com.cosplay;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Database;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) {
        // Ensure database and tables exist before any DAO operations
        Database.init();

        // Init navigation and route to Login
        SceneNavigator.init(stage);
        SceneNavigator.navigate(Views.LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
