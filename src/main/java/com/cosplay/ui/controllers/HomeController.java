package com.cosplay.ui.controllers;

import com.cosplay.ui.Views;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import com.cosplay.dao.FeaturedDAO;
import com.cosplay.dao.EventBannerDAO;
import com.cosplay.model.FeaturedItem;
import com.cosplay.dao.CosplayDAO;

public class HomeController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;
    @FXML private StackPane heroBanner;
    @FXML private Label heroBannerTitle;
    @FXML private Label heroBannerMessage;
    @FXML private ImageView set1Image;
    @FXML private ImageView set2Image;
    @FXML private ImageView set3Image;
    @FXML private ImageView set4Image;
    @FXML private javafx.scene.control.Label set1Title;
    @FXML private javafx.scene.control.Label set2Title;
    @FXML private javafx.scene.control.Label set3Title;
    @FXML private javafx.scene.control.Label set4Title;

    @FXML
    private void initialize() {
        // Set active highlight for nav bar
        if (navBarController != null) {
            navBarController.setActive(Views.HOME);
        }

        // Load hero banner
        loadHeroBanner();

        // Load featured images from DB
        loadFeaturedImages();
    }

    private void loadHeroBanner() {
        EventBannerDAO bannerDAO = new EventBannerDAO();
        bannerDAO.getActiveBanner().ifPresentOrElse(
            banner -> {
                // Set the banner content
                heroBannerTitle.setText(banner.getTitle());
                heroBannerMessage.setText(banner.getMessage());
                
                // Show the banner
                heroBanner.setVisible(true);
                heroBanner.setManaged(true);
            },
            () -> {
                // No active banner - hide it
                heroBanner.setVisible(false);
                heroBanner.setManaged(false);
            }
        );
    }
    
    @FXML
    private void dismissBanner() {
        heroBanner.setVisible(false);
        heroBanner.setManaged(false);
    }

    private void loadFeaturedImages() {
        var dao = new FeaturedDAO();
        setFromItem(set1Image, set1Title, dao.get(1));
        setFromItem(set2Image, set2Title, dao.get(2));
        setFromItem(set3Image, set3Title, dao.get(3));
        setFromItem(set4Image, set4Title, dao.get(4));
    }

    private void setFromItem(ImageView view, javafx.scene.control.Label titleLabel, FeaturedItem item) {
        if (view == null || item == null) return;
        // Prefer cosplay-based image
        if (item.getCosplayId() != null) {
            new CosplayDAO().findById(item.getCosplayId()).ifPresent(c -> {
                String path = c.getImagePath();
                if (path != null && !path.isBlank()) {
                    try {
                        Image image = null;
                        // Check if it's a URL or file path
                        if (path.startsWith("http://") || path.startsWith("https://")) {
                            image = new Image(path, true);
                        } else {
                            // It's a file path - use file:// protocol
                            java.io.File imageFile = new java.io.File(path);
                            if (imageFile.exists()) {
                                image = new Image(imageFile.toURI().toString());
                            } else {
                                System.err.println("Image file not found: " + path);
                            }
                        }
                        if (image != null && !image.isError()) {
                            view.setImage(image);
                        } else {
                            System.err.println("Failed to load image for " + c.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + e.getMessage());
                    }
                }
                String ttl = (item.getTitle() != null && !item.getTitle().isBlank()) ? item.getTitle() : c.getName();
                if (titleLabel != null && ttl != null) titleLabel.setText(ttl);
            });
            return;
        }
        // Fallback legacy URL
        String url = item.getImageUrl();
        if (url != null && !url.isBlank()) {
            try {
                Image image = null;
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    image = new Image(url, true);
                } else {
                    java.io.File imageFile = new java.io.File(url);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                    } else {
                        System.err.println("Image file not found: " + url);
                    }
                }
                if (image != null && !image.isError()) {
                    view.setImage(image);
                } else {
                    System.err.println("Failed to load image from URL: " + url);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
        if (titleLabel != null && item.getTitle() != null && !item.getTitle().isBlank()) {
            titleLabel.setText(item.getTitle());
        }
    }
}

