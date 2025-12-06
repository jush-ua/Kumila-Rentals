package com.cosplay.ui;

import java.io.IOException;
import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

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
        // Don't set default dimensions here - let each view control its own size
    }

    public static void navigate(Views view) {
        ensureInit();
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    SceneNavigator.class.getResource(view.getResource()),
                    "FXML not found: " + view.getResource()
            ));
            Parent root = loader.load();
            // Use the root's preferred size (if provided) to create the Scene so the
            // window opens at the intended dimensions (e.g. 400x500 for LoginView).
            double prefW = root.prefWidth(-1);
            double prefH = root.prefHeight(-1);
            double sceneW = (prefW > 0) ? prefW : 800;
            double sceneH = (prefH > 0) ? prefH : 600;
            Scene scene = new Scene(root, sceneW, sceneH);
            // Attach global stylesheets if present: `app.css` and `styles.css`.
            var appCss = SceneNavigator.class.getResource("/com/cosplay/ui/styles/app.css");
            if (appCss != null) {
                scene.getStylesheets().add(appCss.toExternalForm());
                System.out.println("app.css found: " + appCss.toExternalForm());
            } else {
                System.out.println("app.css not found on classpath");
            }
            var stylesCss = SceneNavigator.class.getResource("/com/cosplay/ui/styles/styles.css");
            if (stylesCss != null) {
                scene.getStylesheets().add(stylesCss.toExternalForm());
                System.out.println("styles.css found: " + stylesCss.toExternalForm());
            } else {
                System.out.println("styles.css not found on classpath");
            }
            
            primaryStage.setTitle(view.getTitle());
            primaryStage.setScene(scene);
            
            // Set minimum window size based on the FXML preferred dimensions
            primaryStage.setMinWidth(sceneW);
            primaryStage.setMinHeight(sceneH);
            
            // Respect per-view resizable flag (configured in Views)
            if (view.isResizable()) {
                primaryStage.setResizable(true);
                // Set a windowed "maximized" default using the primary screen's visual bounds
                try {
                    Rectangle2D vb = Screen.getPrimary().getVisualBounds();
                    double targetW = vb.getWidth() * 0.98; // small margin from absolute edges
                    double targetH = vb.getHeight() * 0.96;
                    primaryStage.setWidth(targetW);
                    primaryStage.setHeight(targetH);
                    primaryStage.setX(vb.getMinX() + (vb.getWidth() - targetW) / 2);
                    primaryStage.setY(vb.getMinY() + (vb.getHeight() - targetH) / 2);
                    primaryStage.setMaximized(false);
                } catch (Exception ignored) {
                    // fallback: do nothing if screen metrics unavailable
                }
            } else {
                // For non-resizable views, fit the stage to the scene size
                primaryStage.setResizable(false);
                primaryStage.setWidth(sceneW);
                primaryStage.setHeight(sceneH);
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
            }
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
