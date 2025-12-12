package com.cosplay.ui.controllers;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.cosplay.dao.CosplayDAO;
import com.cosplay.dao.RentalDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.model.Rental;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import com.cosplay.util.ImageCache;
import com.cosplay.util.AnimationUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class CatalogController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;
    @FXML private FlowPane cosplayGrid;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private TextField searchField;
    
    private final CosplayDAO cosplayDAO = new CosplayDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private java.util.List<Cosplay> allCosplays;
    private java.util.List<Cosplay> filteredCosplays;
    private static final int ITEMS_PER_PAGE = com.cosplay.util.PerformanceConfig.CATALOG_ITEMS_PER_PAGE;
    private int currentlyLoaded = 0;

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.CATALOG);
        }
        
        // Initialize dropdowns
        setupCategoryComboBox();
        setupSortByComboBox();
        
        // Setup search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSortCosplays());
        }
        
        loadCosplays();
    }
    
    private void setupCategoryComboBox() {
        categoryComboBox.getItems().add("All Categories");
        
        // Add subcategories (series) grouped by main category
        var cosplays = cosplayDAO.getAll();
        java.util.Set<String> animeSeriesSet = new java.util.LinkedHashSet<>();
        java.util.Set<String> gameSeriesSet = new java.util.LinkedHashSet<>();
        
        for (Cosplay cosplay : cosplays) {
            if (cosplay.getSeriesName() != null && !cosplay.getSeriesName().isBlank()) {
                String mainCategory = cosplay.getCategory();
                if ("Anime".equalsIgnoreCase(mainCategory)) {
                    animeSeriesSet.add("  ▸ " + cosplay.getSeriesName());
                } else if ("Game".equalsIgnoreCase(mainCategory)) {
                    gameSeriesSet.add("  ▸ " + cosplay.getSeriesName());
                }
            }
        }
        
        // Add Anime category and its subcategories
        categoryComboBox.getItems().add("Anime");
        for (String series : animeSeriesSet) {
            categoryComboBox.getItems().add(series);
        }
        
        // Add Game category and its subcategories
        categoryComboBox.getItems().add("Game");
        for (String series : gameSeriesSet) {
            categoryComboBox.getItems().add(series);
        }
        
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setOnAction(e -> filterAndSortCosplays());
    }
    
    private void setupSortByComboBox() {
        sortByComboBox.getItems().addAll("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)");
        sortByComboBox.setValue("Default");
        sortByComboBox.setOnAction(e -> filterAndSortCosplays());
    }
    
    private void loadCosplays() {
        allCosplays = cosplayDAO.getAll();
        filterAndSortCosplays();
    }
    
    private void filterAndSortCosplays() {
        cosplayGrid.getChildren().clear();
        currentlyLoaded = 0;
        
        // Start with all cosplays
        filteredCosplays = new java.util.ArrayList<>(allCosplays);
        
        // Apply search filter
        String searchText = searchField != null ? searchField.getText() : null;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            filteredCosplays = filteredCosplays.stream()
                .filter(c -> c.getName().toLowerCase().contains(search) ||
                           (c.getSeriesName() != null && c.getSeriesName().toLowerCase().contains(search)) ||
                           (c.getCategory() != null && c.getCategory().toLowerCase().contains(search)))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter by category
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            if (selectedCategory.equals("Anime")) {
                filteredCosplays = filteredCosplays.stream()
                    .filter(c -> "Anime".equalsIgnoreCase(c.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (selectedCategory.equals("Game")) {
                filteredCosplays = filteredCosplays.stream()
                    .filter(c -> "Game".equalsIgnoreCase(c.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (selectedCategory.startsWith("  ▸ ")) {
                // Subcategory (series name)
                String seriesName = selectedCategory.substring(4); // Remove "  ▸ "
                filteredCosplays = filteredCosplays.stream()
                    .filter(c -> seriesName.equals(c.getSeriesName()))
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        // Sort
        String sortBy = sortByComboBox.getValue();
        if (sortBy != null) {
            switch (sortBy) {
                case "Name (A-Z)":
                    filteredCosplays.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    break;
                case "Name (Z-A)":
                    filteredCosplays.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                    break;
                case "Price (Low to High)":
                    filteredCosplays.sort((a, b) -> {
                        Double priceA = a.getRentRate1Day() != null ? a.getRentRate1Day() : Double.MAX_VALUE;
                        Double priceB = b.getRentRate1Day() != null ? b.getRentRate1Day() : Double.MAX_VALUE;
                        return priceA.compareTo(priceB);
                    });
                    break;
                case "Price (High to Low)":
                    filteredCosplays.sort((a, b) -> {
                        Double priceA = a.getRentRate1Day() != null ? a.getRentRate1Day() : 0.0;
                        Double priceB = b.getRentRate1Day() != null ? b.getRentRate1Day() : 0.0;
                        return priceB.compareTo(priceA);
                    });
                    break;
            }
        }
        
        // Load first batch
        loadMoreCosplays();
    }
    
    private void loadMoreCosplays() {
        int toLoad = Math.min(ITEMS_PER_PAGE, filteredCosplays.size() - currentlyLoaded);
        
        for (int i = currentlyLoaded; i < currentlyLoaded + toLoad; i++) {
            VBox card = createCosplayCard(filteredCosplays.get(i));
            cosplayGrid.getChildren().add(card);
            // Stagger the card animations based on their index
            int relativeIndex = i - currentlyLoaded;
            AnimationUtil.fadeInScaleDelayed(card, 300, relativeIndex * 50);
        }
        
        currentlyLoaded += toLoad;
        
        // Add "Load More" button if there are more items
        if (currentlyLoaded < filteredCosplays.size()) {
            Button loadMoreBtn = new Button("Load More (" + (filteredCosplays.size() - currentlyLoaded) + " remaining)");
            loadMoreBtn.setStyle("-fx-background-color: #f79e6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 14px;");
            loadMoreBtn.setOnAction(e -> {
                cosplayGrid.getChildren().remove(loadMoreBtn);
                loadMoreCosplays();
            });
            
            // Center the button
            VBox buttonContainer = new VBox(loadMoreBtn);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPrefWidth(200);
            cosplayGrid.getChildren().add(buttonContainer);
        }
    }
    
    private void loadCosplaysOld() {
        cosplayGrid.getChildren().clear();
        var cosplays = cosplayDAO.getAll();
        
        for (Cosplay cosplay : cosplays) {
            VBox card = createCosplayCard(cosplay);
            cosplayGrid.getChildren().add(card);
        }
    }
    
    private VBox createCosplayCard(Cosplay cosplay) {
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        
        // Make the entire card clickable to view details
        card.setOnMouseClicked(e -> {
            CosplayDetailsController.setSelectedCosplay(cosplay);
            SceneNavigator.navigate(Views.COSPLAY_DETAILS);
        });
        
        // Add hover animation
        AnimationUtil.addCardHoverEffect(card);
        
        // Title label at the top with rounded top corners
        Label nameLabel = new Label(cosplay.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #f79e6b; -fx-padding: 10 15; -fx-background-radius: 15 15 0 0; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setPrefWidth(200);
        card.getChildren().add(nameLabel);
        
        // Image container with rounded bottom corners and border
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 260);
        imageContainer.setMaxSize(200, 260);
        imageContainer.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 15 15; -fx-border-color: #f79e6b; -fx-border-width: 3; -fx-border-radius: 0 0 15 15; -fx-effect: dropshadow(gaussian, rgba(247, 158, 107, 0.5), 10, 0.5, 0, 0);");
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(194);
        imageView.setFitHeight(254);
        imageView.setPreserveRatio(false); // Fill the container completely
        imageView.setSmooth(true); // Enable smooth scaling for high quality
        
        // Clip for rounded corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(194, 254);
        clip.setArcWidth(0);
        clip.setArcHeight(0);
        imageView.setClip(clip);
        
        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        loadingIndicator.setStyle("-fx-progress-color: #f79e6b;");
        imageContainer.getChildren().add(loadingIndicator);
        
        // Load image asynchronously if path exists
        if (cosplay.getImagePath() != null && !cosplay.getImagePath().isBlank()) {
            String imagePath = cosplay.getImagePath();
            
            // Create background task to load image
            Task<Image> loadImageTask = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    // Load image in background thread with caching
                    return ImageCache.getImageScaled(imagePath, 194, 254, true);
                }
            };
            
            // Handle success
            loadImageTask.setOnSucceeded(event -> {
                Image image = loadImageTask.getValue();
                if (image != null && !image.isError()) {
                    imageView.setImage(image);
                    imageContainer.getChildren().clear();
                    imageContainer.getChildren().add(imageView);
                    
                    // Fade in effect
                    imageView.setOpacity(0);
                    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), imageView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                } else {
                    // Show placeholder on error
                    showImagePlaceholder(imageContainer);
                }
            });
            
            // Handle failure
            loadImageTask.setOnFailed(event -> {
                System.err.println("Failed to load image for " + cosplay.getName() + ": " + loadImageTask.getException().getMessage());
                showImagePlaceholder(imageContainer);
            });
            
            // Start the task in background thread
            Thread loadThread = new Thread(loadImageTask);
            loadThread.setDaemon(true);
            loadThread.start();
        } else {
            // No image path, show placeholder immediately
            showImagePlaceholder(imageContainer);
        }
        
        card.getChildren().add(imageContainer);
        
        return card;
    }
    
    private void showImagePlaceholder(StackPane container) {
        Platform.runLater(() -> {
            container.getChildren().clear();
            Label placeholderText = new Label("No Image");
            placeholderText.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            container.getChildren().add(placeholderText);
        });
    }
    
    private void showRentalDialog(Cosplay cosplay) {
        // Check if user is logged in
        if (Session.getCurrentUser() == null) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please login to rent a cosplay.");
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("Rent " + cosplay.getName());
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        
        // Header
        Label titleLabel = new Label("Rent: " + cosplay.getName());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        
        if (cosplay.getSeriesName() != null && !cosplay.getSeriesName().isBlank()) {
            Label seriesLabel = new Label("From: " + cosplay.getSeriesName());
            seriesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            content.getChildren().addAll(titleLabel, seriesLabel);
        } else {
            content.getChildren().add(titleLabel);
        }
        
        Separator sep1 = new Separator();
        content.getChildren().add(sep1);
        
        // Rental Period
        Label lblPeriod = new Label("Rental Period");
        lblPeriod.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        GridPane periodGrid = new GridPane();
        periodGrid.setHgap(10);
        periodGrid.setVgap(10);
        
        Label lblStartDate = new Label("Start Date:");
        DatePicker startDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        startDatePicker.setStyle("-fx-pref-width: 200;");
        
        Label lblEndDate = new Label("End Date:");
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(2));
        endDatePicker.setStyle("-fx-pref-width: 200;");
        
        Label lblTotalCost = new Label("Total Cost: ₱0.00");
        lblTotalCost.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        
        // Calculate cost when dates change
        Runnable calculateCost = () -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            
            if (start != null && end != null && !end.isBefore(start)) {
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                double cost = 0;
                
                if (days == 1 && cosplay.getRentRate1Day() != null) {
                    cost = cosplay.getRentRate1Day();
                } else if (days == 2 && cosplay.getRentRate2Days() != null) {
                    cost = cosplay.getRentRate2Days();
                } else if (days == 3 && cosplay.getRentRate3Days() != null) {
                    cost = cosplay.getRentRate3Days();
                } else if (cosplay.getRentRate1Day() != null) {
                    // Default to daily rate
                    cost = cosplay.getRentRate1Day() * days;
                }
                
                lblTotalCost.setText(String.format("Total Cost: ₱%.2f (%d %s)", cost, days, days == 1 ? "day" : "days"));
            } else {
                lblTotalCost.setText("Total Cost: ₱0.00");
            }
        };
        
        startDatePicker.setOnAction(e -> calculateCost.run());
        endDatePicker.setOnAction(e -> calculateCost.run());
        
        periodGrid.add(lblStartDate, 0, 0);
        periodGrid.add(startDatePicker, 1, 0);
        periodGrid.add(lblEndDate, 0, 1);
        periodGrid.add(endDatePicker, 1, 1);
        
        content.getChildren().addAll(lblPeriod, periodGrid, lblTotalCost);
        
        Separator sep2 = new Separator();
        content.getChildren().add(sep2);
        
        // Customer Information
        Label lblCustomerInfo = new Label("Your Information");
        lblCustomerInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        
        Label lblName = new Label("Full Name:");
        TextField txtName = new TextField(Session.getCurrentUser() != null ? Session.getCurrentUser().getUsername() : "");
        txtName.setPromptText("Enter your full name");
        txtName.setPrefWidth(250);
        
        Label lblContact = new Label("Contact Number:");
        TextField txtContact = new TextField();
        txtContact.setPromptText("e.g., 09123456789");
        txtContact.setPrefWidth(250);
        
        Label lblAddress = new Label("Address:");
        TextArea txtAddress = new TextArea();
        txtAddress.setPromptText("Enter your complete address");
        txtAddress.setPrefRowCount(2);
        txtAddress.setPrefWidth(250);
        txtAddress.setWrapText(true);
        
        Label lblFacebook = new Label("Facebook Profile:");
        TextField txtFacebook = new TextField();
        txtFacebook.setPromptText("Facebook profile URL (optional)");
        txtFacebook.setPrefWidth(250);
        
        infoGrid.add(lblName, 0, 0);
        infoGrid.add(txtName, 1, 0);
        infoGrid.add(lblContact, 0, 1);
        infoGrid.add(txtContact, 1, 1);
        infoGrid.add(lblAddress, 0, 2);
        infoGrid.add(txtAddress, 1, 2);
        infoGrid.add(lblFacebook, 0, 3);
        infoGrid.add(txtFacebook, 1, 3);
        
        content.getChildren().addAll(lblCustomerInfo, infoGrid);
        
        Separator sep3 = new Separator();
        content.getChildren().add(sep3);
        
        // Payment Method
        Label lblPayment = new Label("Payment Method");
        lblPayment.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<String> cboPayment = new ComboBox<>();
        cboPayment.getItems().addAll("GCash", "Maya", "Bank Transfer", "Cash on Pickup");
        cboPayment.setValue("GCash");
        cboPayment.setPrefWidth(250);
        
        content.getChildren().addAll(lblPayment, cboPayment);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnSubmit = new Button("Submit Rental Request");
        btnSubmit.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        
        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ccc; -fx-text-fill: #333; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());
        
        btnSubmit.setOnAction(e -> {
            // Validate inputs
            if (txtName.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please enter your name.");
                return;
            }
            
            if (txtContact.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please enter your contact number.");
                return;
            }
            
            if (txtAddress.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please enter your address.");
                return;
            }
            
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            
            if (start == null || end == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please select rental dates.");
                return;
            }
            
            if (end.isBefore(start)) {
                showAlert(Alert.AlertType.WARNING, "Validation", "End date must be after start date.");
                return;
            }
            
            if (start.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Start date cannot be in the past.");
                return;
            }
            
            // Check availability
            if (!rentalDAO.isAvailable(cosplay.getId(), start, end)) {
                showAlert(Alert.AlertType.ERROR, "Not Available", "This cosplay is not available for the selected dates. Please choose different dates.");
                return;
            }
            
            // Create rental
            Rental rental = new Rental();
            rental.setCosplayId(cosplay.getId());
            rental.setCustomerName(txtName.getText().trim());
            rental.setContactNumber(txtContact.getText().trim());
            rental.setAddress(txtAddress.getText().trim());
            rental.setFacebookLink(txtFacebook.getText().trim());
            rental.setStartDate(start);
            rental.setEndDate(end);
            rental.setPaymentMethod(cboPayment.getValue());
            rental.setProofOfPayment(""); // To be uploaded later
            rental.setStatus("Pending");
            
            boolean success = rentalDAO.createRental(rental);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Your rental request has been submitted successfully!\n\n" +
                    "Rental ID: " + rental.getId() + "\n" +
                    "Status: Pending\n\n" +
                    "We will contact you shortly for payment confirmation.");
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create rental. The cosplay might have been booked by someone else.");
            }
        });
        
        buttonBox.getChildren().addAll(btnSubmit, btnCancel);
        content.getChildren().add(buttonBox);
        
        // Calculate initial cost
        calculateCost.run();
        
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 550, 700);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
}
