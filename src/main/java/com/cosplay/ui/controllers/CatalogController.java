package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.dao.CosplayDAO;
import com.cosplay.dao.RentalDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.model.Rental;
import com.cosplay.util.Session;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CatalogController {
    // Included NavBar controller (from fx:include with fx:id="navBar")
    @FXML private NavController navBarController;
    @FXML private FlowPane cosplayGrid;
    
    private final CosplayDAO cosplayDAO = new CosplayDAO();
    private final RentalDAO rentalDAO = new RentalDAO();

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.CATALOG);
        }
        loadCosplays();
    }
    
    private void loadCosplays() {
        cosplayGrid.getChildren().clear();
        var cosplays = cosplayDAO.getAll();
        
        for (Cosplay cosplay : cosplays) {
            VBox card = createCosplayCard(cosplay);
            cosplayGrid.getChildren().add(card);
        }
    }
    
    private VBox createCosplayCard(Cosplay cosplay) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        
        if (cosplay.getImagePath() != null && !cosplay.getImagePath().isBlank()) {
            try {
                String imagePath = cosplay.getImagePath();
                Image image;
                
                // Check if it's a file path or URL
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    image = new Image(imagePath, true);
                } else {
                    // It's a file path - use file:// protocol
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                    } else {
                        image = null;
                    }
                }
                
                if (image != null) {
                    imageView.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image for " + cosplay.getName() + ": " + e.getMessage());
            }
        }
        
        // Name
        Label nameLabel = new Label(cosplay.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        
        card.getChildren().addAll(imageView, nameLabel);
        
        // Series Name
        if (cosplay.getSeriesName() != null && !cosplay.getSeriesName().isBlank()) {
            Label seriesLabel = new Label(cosplay.getSeriesName());
            seriesLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-style: italic;");
            card.getChildren().add(seriesLabel);
        }
        
        // Category
        if (cosplay.getCategory() != null && !cosplay.getCategory().isBlank()) {
            Label categoryLabel = new Label(cosplay.getCategory());
            categoryLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            card.getChildren().add(categoryLabel);
        }
        
        // Size
        if (cosplay.getSize() != null && !cosplay.getSize().isBlank()) {
            Label sizeLabel = new Label("Size: " + cosplay.getSize());
            sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
            card.getChildren().add(sizeLabel);
        }
        
        // Rent Rate (show 1 day rate if available)
        if (cosplay.getRentRate1Day() != null) {
            Label priceLabel = new Label(String.format("₱%.2f/day", cosplay.getRentRate1Day()));
            priceLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
            card.getChildren().add(priceLabel);
        }
        
        // Rent Button
        Button rentButton = new Button("Rent Now");
        rentButton.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 20;");
        rentButton.setMaxWidth(Double.MAX_VALUE);
        rentButton.setOnAction(e -> showRentalDialog(cosplay));
        
        rentButton.setOnMouseEntered(e -> rentButton.setStyle("-fx-background-color: #d49940; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 20;"));
        rentButton.setOnMouseExited(e -> rentButton.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 20;"));
        
        card.getChildren().add(rentButton);
        
        return card;
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
                    "Your rental request has been submitted successfully!\\n\\n" +
                    "Rental ID: " + rental.getId() + "\\n" +
                    "Status: Pending\\n\\n" +
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
