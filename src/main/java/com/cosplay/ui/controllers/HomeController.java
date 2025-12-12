package com.cosplay.ui.controllers;

import com.cosplay.ui.Views;
import com.cosplay.ui.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import com.cosplay.dao.FeaturedDAO;
import com.cosplay.dao.EventBannerDAO;
import com.cosplay.model.FeaturedItem;
import com.cosplay.dao.CosplayDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.util.ImageCache;
import com.cosplay.util.AnimationUtil;

public class HomeController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;
    @FXML private StackPane heroBanner;
    @FXML private ImageView bannerImageView;
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
    @FXML private VBox set1Card;
    @FXML private VBox set2Card;
    @FXML private VBox set3Card;
    @FXML private VBox set4Card;
    @FXML private HBox featuredCardsContainer;
    @FXML private Label emptyFeaturedLabel;
    
    private FeaturedItem[] featuredItems = new FeaturedItem[4];
    private StackPane[] imageContainers = new StackPane[4];

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
        
        // Add click handlers to featured cards
        setupFeaturedCardHandlers();
    }
    
    private void setupFeaturedCardHandlers() {
        if (set1Card != null) {
            set1Card.setOnMouseClicked(e -> openFeaturedCosplay(0));
            AnimationUtil.addCardHoverEffect(set1Card);
        }
        if (set2Card != null) {
            set2Card.setOnMouseClicked(e -> openFeaturedCosplay(1));
            AnimationUtil.addCardHoverEffect(set2Card);
        }
        if (set3Card != null) {
            set3Card.setOnMouseClicked(e -> openFeaturedCosplay(2));
            AnimationUtil.addCardHoverEffect(set3Card);
        }
        if (set4Card != null) {
            set4Card.setOnMouseClicked(e -> openFeaturedCosplay(3));
            AnimationUtil.addCardHoverEffect(set4Card);
        }
    }
    
    private void openFeaturedCosplay(int index) {
        FeaturedItem item = featuredItems[index];
        if (item == null || item.getCosplayId() == null) return;
        
        CosplayDAO dao = new CosplayDAO();
        dao.findById(item.getCosplayId()).ifPresent(cosplay -> {
            CosplayDetailsController.setSelectedCosplay(cosplay);
            SceneNavigator.navigate(Views.COSPLAY_DETAILS);
        });
    }

    private void loadHeroBanner() {
        EventBannerDAO bannerDAO = new EventBannerDAO();
        bannerDAO.getActiveBanner().ifPresentOrElse(
            banner -> {
                // Set the banner title
                heroBannerTitle.setText(banner.getTitle());
                
                // Show subtitle if available
                if (banner.getSubtitle() != null && !banner.getSubtitle().isBlank()) {
                    heroBannerMessage.setText(banner.getSubtitle());
                } else {
                    heroBannerMessage.setText(banner.getMessage());
                }
                
                // Set background image if available
                if (banner.getImagePath() != null && !banner.getImagePath().isBlank()) {
                    try {
                        javafx.scene.image.Image bgImage = ImageCache.getImage(banner.getImagePath(), true);
                        if (bgImage != null && !bgImage.isError()) {
                            bannerImageView.setImage(bgImage);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load banner image: " + e.getMessage());
                    }
                }
                
                // Make banner clickable to show event details
                heroBanner.setOnMouseClicked(e -> showEventDetails(banner));
                heroBanner.setStyle(heroBanner.getStyle() + "; -fx-cursor: hand;");
                
                // Show the banner with animation
                heroBanner.setVisible(true);
                heroBanner.setManaged(true);
                AnimationUtil.slideInFromBottom(heroBanner, 600);
            },
            () -> {
                // No active banner - hide it
                heroBanner.setVisible(false);
                heroBanner.setManaged(false);
            }
        );
    }
    
    private void showEventDetails(com.cosplay.model.EventBanner banner) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(banner.getEventName() != null ? banner.getEventName() : banner.getTitle());
        
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(20);
        content.setPadding(new javafx.geometry.Insets(30));
        content.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white;");
        
        // Event image
        if (banner.getImagePath() != null && !banner.getImagePath().isBlank()) {
            try {
                javafx.scene.image.Image eventImage = ImageCache.getImage(banner.getImagePath(), true);
                if (eventImage != null && !eventImage.isError()) {
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(eventImage);
                    imageView.setFitWidth(500);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    
                    javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane(imageView);
                    imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
                    imageContainer.setPadding(new javafx.geometry.Insets(10));
                    content.getChildren().add(imageContainer);
                }
            } catch (Exception e) {
                System.err.println("Failed to load event image: " + e.getMessage());
            }
        }
        
        // Event Name
        if (banner.getEventName() != null && !banner.getEventName().isBlank()) {
            javafx.scene.control.Label lblEventName = new javafx.scene.control.Label(banner.getEventName());
            lblEventName.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
            lblEventName.setWrapText(true);
            lblEventName.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().add(lblEventName);
        }
        
        // Venue
        if (banner.getVenue() != null && !banner.getVenue().isBlank()) {
            HBox venueBox = new HBox(10);
            venueBox.setAlignment(javafx.geometry.Pos.CENTER);
            javafx.scene.control.Label venueIcon = new javafx.scene.control.Label("📍");
            venueIcon.setStyle("-fx-font-size: 20px;");
            javafx.scene.control.Label lblVenue = new javafx.scene.control.Label(banner.getVenue());
            lblVenue.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            venueBox.getChildren().addAll(venueIcon, lblVenue);
            content.getChildren().add(venueBox);
        }
        
        // Onsite Rent Date
        if (banner.getOnsiteRentDate() != null && !banner.getOnsiteRentDate().isBlank()) {
            HBox dateBox = new HBox(10);
            dateBox.setAlignment(javafx.geometry.Pos.CENTER);
            javafx.scene.control.Label dateIcon = new javafx.scene.control.Label("📅");
            dateIcon.setStyle("-fx-font-size: 20px;");
            javafx.scene.control.Label lblDate = new javafx.scene.control.Label("Onsite Rent: " + banner.getOnsiteRentDate());
            lblDate.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-font-weight: bold;");
            dateBox.getChildren().addAll(dateIcon, lblDate);
            content.getChildren().add(dateBox);
        }
        
        // Close button
        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("Close");
        btnClose.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 40; -fx-background-radius: 25; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());
        content.getChildren().add(btnClose);
        
        scrollPane.setContent(content);
        javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane, 600, 700);
        dialog.setScene(scene);
        dialog.show();
    }
    
    @FXML
    private void dismissBanner() {
        heroBanner.setVisible(false);
        heroBanner.setManaged(false);
    }

    private void loadFeaturedImages() {
        var dao = new FeaturedDAO();
        featuredItems[0] = dao.get(1);
        featuredItems[1] = dao.get(2);
        featuredItems[2] = dao.get(3);
        featuredItems[3] = dao.get(4);
        
        // Check if all featured items are empty
        boolean allEmpty = true;
        for (FeaturedItem item : featuredItems) {
            if (item != null && (item.getCosplayId() != null || 
                (item.getImageUrl() != null && !item.getImageUrl().isBlank()))) {
                allEmpty = false;
                break;
            }
        }
        
        // Show appropriate UI based on whether all items are empty
        if (allEmpty) {
            // Hide all cards and show empty state message
            if (featuredCardsContainer != null) {
                featuredCardsContainer.setVisible(false);
                featuredCardsContainer.setManaged(false);
            }
            if (emptyFeaturedLabel != null) {
                emptyFeaturedLabel.setVisible(true);
                emptyFeaturedLabel.setManaged(true);
            }
        } else {
            // Show cards and hide empty state message
            if (featuredCardsContainer != null) {
                featuredCardsContainer.setVisible(true);
                featuredCardsContainer.setManaged(true);
            }
            if (emptyFeaturedLabel != null) {
                emptyFeaturedLabel.setVisible(false);
                emptyFeaturedLabel.setManaged(false);
            }
            
            // Get StackPane containers from the card VBoxes
            imageContainers[0] = (StackPane) set1Card.getChildren().get(0);
            imageContainers[1] = (StackPane) set2Card.getChildren().get(0);
            imageContainers[2] = (StackPane) set3Card.getChildren().get(0);
            imageContainers[3] = (StackPane) set4Card.getChildren().get(0);
            
            setFromItem(set1Image, set1Title, featuredItems[0], imageContainers[0]);
            setFromItem(set2Image, set2Title, featuredItems[1], imageContainers[1]);
            setFromItem(set3Image, set3Title, featuredItems[2], imageContainers[2]);
            setFromItem(set4Image, set4Title, featuredItems[3], imageContainers[3]);
            
            // Animate cards with staggered effect
            AnimationUtil.fadeInScaleDelayed(set1Card, 400, 100);
            AnimationUtil.fadeInScaleDelayed(set2Card, 400, 200);
            AnimationUtil.fadeInScaleDelayed(set3Card, 400, 300);
            AnimationUtil.fadeInScaleDelayed(set4Card, 400, 400);
        }
    }

    private void setFromItem(ImageView view, javafx.scene.control.Label titleLabel, FeaturedItem item, StackPane container) {
        if (view == null) return;
        
        // Check if there's no featured item
        if (item == null || (item.getCosplayId() == null && (item.getImageUrl() == null || item.getImageUrl().isBlank()))) {
            // Show placeholder with gray background on the StackPane
            view.setImage(null); // Clear any existing image
            if (container != null) {
                // Set gray background on the container
                container.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 15; -fx-border-radius: 15;");
            }
            if (titleLabel != null) {
                titleLabel.setText("No Featured Set");
                titleLabel.setStyle("-fx-text-fill: #999; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-color: white; -fx-background-radius: 20; -fx-padding: 8 20;");
            }
            return;
        }
        
        // Prefer cosplay-based image
        if (item.getCosplayId() != null) {
            new CosplayDAO().findById(item.getCosplayId()).ifPresent(c -> {
                String path = c.getImagePath();
                if (path != null && !path.isBlank()) {
                    try {
                        // Use ImageCache with higher resolution (280x400) for better quality
                        Image image = ImageCache.getImageScaled(path, 280, 400, true);
                        if (image != null && !image.isError()) {
                            view.setImage(image);
                            // Reset container style when image loads successfully
                            if (container != null) {
                                container.setStyle("-fx-background-radius: 15; -fx-border-radius: 15; -fx-background-clip: padding-box; -fx-effect: dropshadow(gaussian, rgba(255, 255, 255, 1), 15, 0.7, 0, 0);");
                            }
                        } else {
                            System.err.println("Failed to load image for " + c.getName());
                            setPlaceholderImage(view, titleLabel, container);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + e.getMessage());
                        setPlaceholderImage(view, titleLabel, container);
                    }
                } else {
                    setPlaceholderImage(view, titleLabel, container);
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
                Image image = ImageCache.getImageScaled(url, 280, 400, true);
                if (image != null && !image.isError()) {
                    view.setImage(image);
                    // Reset container style when image loads successfully
                    if (container != null) {
                        container.setStyle("-fx-background-radius: 15; -fx-border-radius: 15; -fx-background-clip: padding-box; -fx-effect: dropshadow(gaussian, rgba(255, 255, 255, 1), 15, 0.7, 0, 0);");
                    }
                } else {
                    System.err.println("Failed to load image from URL: " + url);
                    setPlaceholderImage(view, titleLabel, container);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
                setPlaceholderImage(view, titleLabel, container);
            }
        } else {
            setPlaceholderImage(view, titleLabel, container);
        }
        if (titleLabel != null && item.getTitle() != null && !item.getTitle().isBlank()) {
            titleLabel.setText(item.getTitle());
        }
    }
    
    private void setPlaceholderImage(ImageView view, javafx.scene.control.Label titleLabel, StackPane container) {
        if (view != null) {
            view.setImage(null);
        }
        if (container != null) {
            // Set gray background on the container
            container.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 15; -fx-border-radius: 15;");
        }
        if (titleLabel != null) {
            titleLabel.setText("No Featured Set");
            titleLabel.setStyle("-fx-text-fill: #999; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-color: white; -fx-background-radius: 20; -fx-padding: 8 20;");
        }
    }
}

