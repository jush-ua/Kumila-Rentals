package com.cosplay.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtil {
    
    /**
     * Fade in animation
     */
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Fade out animation
     */
    public static void fadeOut(Node node, double durationMs, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        fade.play();
    }
    
    /**
     * Fade in with scale animation (good for cards/dialogs)
     */
    public static void fadeInScale(Node node, double durationMs) {
        node.setOpacity(0);
        node.setScaleX(0.8);
        node.setScaleY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
    }
    
    /**
     * Slide in from bottom animation
     */
    public static void slideInFromBottom(Node node, double durationMs) {
        node.setOpacity(0);
        node.setTranslateY(30);
        
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(30);
        slide.setToY(0);
        
        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.play();
    }
    
    /**
     * Delayed fade in (for staggered effects)
     */
    public static void fadeInDelayed(Node node, double durationMs, double delayMs) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setDelay(Duration.millis(delayMs));
        fade.play();
    }
    
    /**
     * Delayed fade in with scale (for staggered card effects)
     */
    public static void fadeInScaleDelayed(Node node, double durationMs, double delayMs) {
        node.setOpacity(0);
        node.setScaleX(0.9);
        node.setScaleY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setDelay(Duration.millis(delayMs));
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setDelay(Duration.millis(delayMs));
        
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
    }
    
    /**
     * Button hover scale effect
     */
    public static void addButtonHoverEffect(Node button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }
    
    /**
     * Card hover effect (gentle lift)
     */
    public static void addCardHoverEffect(Node card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.03);
            scale.setToY(1.03);
            
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), card);
            translate.setToY(-5);
            
            ParallelTransition parallel = new ParallelTransition(scale, translate);
            parallel.play();
        });
        
        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            
            TranslateTransition translate = new TranslateTransition(Duration.millis(200), card);
            translate.setToY(0);
            
            ParallelTransition parallel = new ParallelTransition(scale, translate);
            parallel.play();
        });
    }
}
