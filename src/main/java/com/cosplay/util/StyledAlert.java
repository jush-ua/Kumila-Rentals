package com.cosplay.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

/**
 * Utility class for creating styled alert dialogs with modern design.
 */
public class StyledAlert {
    
    // Brand colors
    private static final String ORANGE_PRIMARY = "#F7A84C";
    private static final String ORANGE_ACCENT = "#E89530";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String ERROR_COLOR = "#f44336";
    private static final String WARNING_COLOR = "#FF9800";
    private static final String INFO_COLOR = "#2196F3";
    
    /**
     * Create a styled information alert.
     */
    public static Alert createInfo(String title, String message) {
        return createAlert(Alert.AlertType.INFORMATION, title, message, INFO_COLOR);
    }
    
    /**
     * Create a styled success alert.
     */
    public static Alert createSuccess(String title, String message) {
        return createAlert(Alert.AlertType.INFORMATION, title, message, SUCCESS_COLOR);
    }
    
    /**
     * Create a styled error alert.
     */
    public static Alert createError(String title, String message) {
        return createAlert(Alert.AlertType.ERROR, title, message, ERROR_COLOR);
    }
    
    /**
     * Create a styled warning alert.
     */
    public static Alert createWarning(String title, String message) {
        return createAlert(Alert.AlertType.WARNING, title, message, WARNING_COLOR);
    }
    
    /**
     * Create a styled confirmation alert.
     */
    public static Alert createConfirmation(String title, String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        styleAlert(alert, ORANGE_PRIMARY);
        return alert;
    }
    
    /**
     * Create a styled alert with custom type and color.
     */
    private static Alert createAlert(Alert.AlertType type, String title, String message, String accentColor) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, accentColor);
        return alert;
    }
    
    /**
     * Apply modern styling to an alert dialog.
     */
    private static void styleAlert(Alert alert, String accentColor) {
        DialogPane dialogPane = alert.getDialogPane();
        
        // Apply CSS styling
        String css = String.format("""
            .dialog-pane {
                -fx-background-color: white;
                -fx-background-radius: 12px;
                -fx-border-radius: 12px;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 15, 0, 0, 3);
                -fx-padding: 0;
            }
            
            .dialog-pane > .header-panel {
                -fx-background-color: %s;
                -fx-background-radius: 12px 12px 0 0;
                -fx-padding: 20px 25px;
            }
            
            .dialog-pane > .header-panel > .label {
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
            }
            
            .dialog-pane > .content {
                -fx-padding: 25px;
                -fx-background-color: white;
            }
            
            .dialog-pane > .content > .label {
                -fx-text-fill: #333;
                -fx-font-size: 15px;
                -fx-line-spacing: 4px;
            }
            
            .dialog-pane > .button-bar {
                -fx-padding: 15px 25px 20px 25px;
                -fx-background-color: #FAFAFA;
                -fx-background-radius: 0 0 12px 12px;
            }
            
            .dialog-pane > .button-bar > .button {
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: 600;
                -fx-padding: 10px 25px;
                -fx-background-radius: 8px;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 1);
            }
            
            .dialog-pane > .button-bar > .button:hover {
                -fx-background-color: %s;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 6, 0, 0, 2);
            }
            
            .dialog-pane > .button-bar > .button:pressed {
                -fx-background-color: %s;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 3, 0, 0, 1);
            }
            
            .dialog-pane > .button-bar > .button.cancel-button {
                -fx-background-color: #E0E0E0;
                -fx-text-fill: #666;
            }
            
            .dialog-pane > .button-bar > .button.cancel-button:hover {
                -fx-background-color: #CACACA;
            }
            """, 
            accentColor,
            accentColor,
            adjustBrightness(accentColor, -0.1),
            adjustBrightness(accentColor, -0.2)
        );
        
        dialogPane.setStyle(css);
        
        // Remove default window decorations for cleaner look
        alert.initStyle(StageStyle.TRANSPARENT);
    }
    
    /**
     * Adjust color brightness for hover effects.
     */
    private static String adjustBrightness(String hexColor, double factor) {
        // Simple brightness adjustment - darken for negative factor
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);
        
        r = Math.max(0, Math.min(255, (int)(r + (r * factor))));
        g = Math.max(0, Math.min(255, (int)(g + (g * factor))));
        b = Math.max(0, Math.min(255, (int)(b + (b * factor))));
        
        return String.format("#%02X%02X%02X", r, g, b);
    }
    
    /**
     * Show an information alert and wait for response.
     */
    public static void showInfo(String title, String message) {
        createInfo(title, message).showAndWait();
    }
    
    /**
     * Show a success alert and wait for response.
     */
    public static void showSuccess(String title, String message) {
        createSuccess(title, message).showAndWait();
    }
    
    /**
     * Show an error alert and wait for response.
     */
    public static void showError(String title, String message) {
        createError(title, message).showAndWait();
    }
    
    /**
     * Show a warning alert and wait for response.
     */
    public static void showWarning(String title, String message) {
        createWarning(title, message).showAndWait();
    }
}
