package com.cosplay;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.CallbackServer;
import com.cosplay.util.Database;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    private static CallbackServer callbackServer;
    
    @Override
    public void start(Stage stage) {
        // Ensure database and tables exist before any DAO operations
        Database.init();
        
        // Start callback server for OAuth and email verification
        try {
            callbackServer = new CallbackServer();
            callbackServer.start();
            System.out.println("Callback server started successfully");
        } catch (Exception e) {
            System.err.println("Failed to start callback server: " + e.getMessage());
            e.printStackTrace();
        }

        // Init navigation and route to Login
        SceneNavigator.init(stage);
        SceneNavigator.navigate(Views.LOGIN);
        
        // Stop callback server when application closes
        stage.setOnCloseRequest(event -> {
            if (callbackServer != null) {
                callbackServer.stop();
            }
            // Close database connection pool
            Database.close();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Get the callback server instance.
     * @return the callback server
     */
    public static CallbackServer getCallbackServer() {
        return callbackServer;
    }
}
