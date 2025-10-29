package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.cosplay.dao.FeaturedDAO;
import com.cosplay.model.FeaturedItem;
import com.cosplay.dao.CostumeDAO;

public class HomeController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;
    @FXML private ImageView jtLogo;
    @FXML private ImageView lalamoveLogo;
    @FXML private ImageView lbcLogo;
    @FXML private ImageView gcashLogo;
    @FXML private ImageView mayaLogo;
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

        // Load featured images from DB
        loadFeaturedImages();
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
        // Prefer costume-based image
        if (item.getCostumeId() != null) {
            new CostumeDAO().findById(item.getCostumeId()).ifPresent(c -> {
                String path = c.getImagePath();
                if (path != null && !path.isBlank()) {
                    try { view.setImage(new Image(path, true)); } catch (Exception ignored) {}
                }
                String ttl = (item.getTitle() != null && !item.getTitle().isBlank()) ? item.getTitle() : c.getName();
                if (titleLabel != null && ttl != null) titleLabel.setText(ttl);
            });
            return;
        }
        // Fallback legacy URL
        String url = item.getImageUrl();
        if (url != null && !url.isBlank()) {
            try { view.setImage(new Image(url, true)); } catch (Exception ignored) {}
        }
        if (titleLabel != null && item.getTitle() != null && !item.getTitle().isBlank()) {
            titleLabel.setText(item.getTitle());
        }
    }
}
