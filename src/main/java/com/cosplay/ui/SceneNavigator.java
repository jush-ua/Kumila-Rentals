package com.cosplay.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Simple navigator to switch between JavaFX views.
 * Usage: SceneNavigator.init(primaryStage); SceneNavigator.navigate(Views.LOGIN);
 */
public final class SceneNavigator {
    private static Stage primaryStage;
    private static Views currentView;

    private SceneNavigator() {}

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
    }

    public static void navigate(Views view) {
        ensureInit();
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    SceneNavigator.class.getResource(view.getResource()),
                    "FXML not found: " + view.getResource()
            ));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            // Attach app stylesheet if present
            var css = SceneNavigator.class.getResource("/com/cosplay/ui/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            primaryStage.setTitle(view.getTitle());
            primaryStage.setScene(scene);
            primaryStage.show();
            currentView = view;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + view, e);
        }
    }

    public static Views getCurrentView() {
        return currentView;
    }

    private static void ensureInit() {
        if (primaryStage == null) throw new IllegalStateException("SceneNavigator not initialized");
    }
}
