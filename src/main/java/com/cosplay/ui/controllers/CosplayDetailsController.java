package com.cosplay.ui.controllers;

import com.cosplay.dao.CosplayDAO;
import com.cosplay.dao.RentalDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.model.Rental;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CosplayDetailsController {
    @FXML private NavController navBarController;
    @FXML private ImageView cosplayImage;
    @FXML private Label lblCosplayName;
    @FXML private Label lblSeriesName;
    @FXML private Label lblCategory;
    @FXML private Label lblSize;
    @FXML private Label lblDescription;
    @FXML private VBox inclusionsBox;
    @FXML private Label lblRate1Day;
    @FXML private Label lblRate2Days;
    @FXML private Label lblRate3Days;
    @FXML private Button btnRentNow;
    @FXML private GridPane calendarGrid;
    @FXML private Label lblCalendarMonth;
    @FXML private Button btnPrevMonth;
    @FXML private Button btnNextMonth;
    
    private final CosplayDAO cosplayDAO = new CosplayDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private static Cosplay selectedCosplay;
    private YearMonth currentMonth;
    private Set<LocalDate> bookedDates;
    
    public static void setSelectedCosplay(Cosplay cosplay) {
        selectedCosplay = cosplay;
    }
    
    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.CATALOG);
        }
        
        currentMonth = YearMonth.now();
        
        if (selectedCosplay != null) {
            loadCosplayDetails(selectedCosplay);
            loadBookedDates();
            updateCalendar();
        }
    }
    
    private void loadBookedDates() {
        bookedDates = new HashSet<>();
        List<Rental> rentals = rentalDAO.getRentalsByCosplayId(selectedCosplay.getId());
        
        for (Rental rental : rentals) {
            LocalDate date = rental.getStartDate();
            while (!date.isAfter(rental.getEndDate())) {
                bookedDates.add(date);
                date = date.plusDays(1);
            }
        }
    }
    
    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendar();
    }
    
    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendar();
    }
    
    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        
        lblCalendarMonth.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
        
        // Add day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #666;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Get first day of month
        LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0
        
        // Fill calendar
        int daysInMonth = currentMonth.lengthOfMonth();
        int row = 1;
        int col = startDayOfWeek;
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Label dayCell = new Label(String.valueOf(day));
            dayCell.setPrefSize(40, 40);
            dayCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayCell.setAlignment(Pos.CENTER);
            
            // Style based on availability
            boolean isPast = date.isBefore(LocalDate.now());
            boolean isBooked = bookedDates.contains(date);
            boolean isToday = date.equals(LocalDate.now());
            
            if (isPast) {
                dayCell.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #ccc; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
            } else if (isBooked) {
                dayCell.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
            } else if (isToday) {
                dayCell.setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #333; -fx-font-weight: bold; -fx-border-color: #fbc02d; -fx-border-width: 2;");
            } else {
                dayCell.setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
            
            calendarGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }
    
    private void loadCosplayDetails(Cosplay cosplay) {
        // Load image
        if (cosplay.getImagePath() != null && !cosplay.getImagePath().isBlank()) {
            try {
                String imagePath = cosplay.getImagePath();
                Image image = null;
                
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    image = new Image(imagePath, true);
                } else {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                    }
                }
                
                if (image != null && !image.isError()) {
                    cosplayImage.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
        
        // Set text fields
        lblCosplayName.setText(cosplay.getName());
        
        if (cosplay.getSeriesName() != null && !cosplay.getSeriesName().isBlank()) {
            lblSeriesName.setText(cosplay.getSeriesName());
            lblSeriesName.setVisible(true);
        } else {
            lblSeriesName.setVisible(false);
        }
        
        if (cosplay.getCategory() != null && !cosplay.getCategory().isBlank()) {
            lblCategory.setText("Category: " + cosplay.getCategory());
        } else {
            lblCategory.setText("Category: N/A");
        }
        
        if (cosplay.getSize() != null && !cosplay.getSize().isBlank()) {
            lblSize.setText("Size: " + cosplay.getSize());
        } else {
            lblSize.setText("Size: N/A");
        }
        
        if (cosplay.getDescription() != null && !cosplay.getDescription().isBlank()) {
            lblDescription.setText(cosplay.getDescription());
            lblDescription.setVisible(true);
        } else {
            lblDescription.setVisible(false);
        }
        
        // Load inclusions/add-ons
        inclusionsBox.getChildren().clear();
        if (cosplay.getAddOns() != null && !cosplay.getAddOns().isBlank()) {
            String[] addOns = cosplay.getAddOns().split("\n");
            for (String addOn : addOns) {
                if (!addOn.trim().isEmpty()) {
                    Label addOnLabel = new Label("• " + addOn.trim());
                    addOnLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                    inclusionsBox.getChildren().add(addOnLabel);
                }
            }
        }
        
        // Set rental rates
        if (cosplay.getRentRate1Day() != null) {
            lblRate1Day.setText(String.format("₱%.2f", cosplay.getRentRate1Day()));
        } else {
            lblRate1Day.setText("N/A");
        }
        
        if (cosplay.getRentRate2Days() != null) {
            lblRate2Days.setText(String.format("₱%.2f", cosplay.getRentRate2Days()));
        } else {
            lblRate2Days.setText("N/A");
        }
        
        if (cosplay.getRentRate3Days() != null) {
            lblRate3Days.setText(String.format("₱%.2f", cosplay.getRentRate3Days()));
        } else {
            lblRate3Days.setText("N/A");
        }
    }
    
    @FXML
    private void handleRentNow() {
        if (selectedCosplay != null) {
            showRentalDialog(selectedCosplay);
        }
    }
    
    @FXML
    private void goBack() {
        SceneNavigator.navigate(Views.CATALOG);
    }
    
    private void showRentalDialog(Cosplay cosplay) {
        // Check if user is logged in
        if (Session.getCurrentUser() == null) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please login to rent a cosplay.");
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("Order Form");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2c2c2c;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        content.setMaxWidth(400);
        content.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label titleLabel = new Label("ORDER FORM");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #f5c469; -fx-padding: 8 40; -fx-background-radius: 5;");
        titleLabel.setAlignment(Pos.CENTER);
        
        // Costume Set
        Label lblCostumeSet = new Label("Costume Set:");
        lblCostumeSet.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label lblCostumeName = new Label(cosplay.getName() + (cosplay.getSeriesName() != null ? " - " + cosplay.getSeriesName() : ""));
        lblCostumeName.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-padding: 0 0 0 20;");
        
        // No. of Rent Day
        Label lblRentDays = new Label("No. of Rent Day:");
        lblRentDays.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        ToggleGroup daysGroup = new ToggleGroup();
        RadioButton rb1Day = new RadioButton("1 day");
        RadioButton rb2Days = new RadioButton("2 days");
        RadioButton rb3Days = new RadioButton("3 days");
        
        rb1Day.setToggleGroup(daysGroup);
        rb2Days.setToggleGroup(daysGroup);
        rb3Days.setToggleGroup(daysGroup);
        rb1Day.setSelected(true);
        
        rb1Day.setStyle("-fx-font-size: 11px;");
        rb2Days.setStyle("-fx-font-size: 11px;");
        rb3Days.setStyle("-fx-font-size: 11px;");
        
        VBox daysBox = new VBox(5);
        daysBox.getChildren().addAll(rb1Day, rb2Days, rb3Days);
        daysBox.setPadding(new Insets(0, 0, 0, 20));
        
        // Add Ons
        Label lblAddOns = new Label("Add Ons:");
        lblAddOns.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        TextArea txtAddOns = new TextArea();
        txtAddOns.setPromptText("List any additional items needed...");
        txtAddOns.setPrefRowCount(3);
        txtAddOns.setMaxWidth(Double.MAX_VALUE);
        txtAddOns.setStyle("-fx-font-size: 11px; -fx-border-color: #ddd; -fx-border-radius: 3;");
        
        // Date Fields
        HBox datesBox = new HBox(10);
        datesBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox startDateBox = new VBox(5);
        Label lblStartDate = new Label("Start Rent Date");
        lblStartDate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        DatePicker startDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        startDatePicker.setStyle("-fx-font-size: 10px;");
        startDatePicker.setPrefWidth(140);
        startDateBox.getChildren().addAll(lblStartDate, startDatePicker);
        
        VBox endDateBox = new VBox(5);
        Label lblEndDate = new Label("End Rent Date");
        lblEndDate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(2));
        endDatePicker.setStyle("-fx-font-size: 10px;");
        endDatePicker.setPrefWidth(140);
        endDateBox.getChildren().addAll(lblEndDate, endDatePicker);
        
        datesBox.getChildren().addAll(startDateBox, endDateBox);
        
        // Auto-update end date based on rental days
        Runnable updateEndDate = () -> {
            if (startDatePicker.getValue() != null) {
                int days = 1;
                if (rb2Days.isSelected()) days = 2;
                else if (rb3Days.isSelected()) days = 3;
                endDatePicker.setValue(startDatePicker.getValue().plusDays(days - 1));
            }
        };
        
        rb1Day.setOnAction(e -> updateEndDate.run());
        rb2Days.setOnAction(e -> updateEndDate.run());
        rb3Days.setOnAction(e -> updateEndDate.run());
        startDatePicker.setOnAction(e -> updateEndDate.run());
        
        // Mode of Delivery
        Label lblDelivery = new Label("Mode of Delivery");
        lblDelivery.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        ToggleGroup deliveryGroup = new ToggleGroup();
        RadioButton rbLBC = new RadioButton("LBC");
        RadioButton rbJT = new RadioButton("J&T");
        RadioButton rbLalamove = new RadioButton("Lalamove");
        RadioButton rbOnsite = new RadioButton("Onsite");
        RadioButton rbOthers = new RadioButton("Others");
        
        rbLBC.setToggleGroup(deliveryGroup);
        rbJT.setToggleGroup(deliveryGroup);
        rbLalamove.setToggleGroup(deliveryGroup);
        rbOnsite.setToggleGroup(deliveryGroup);
        rbOthers.setToggleGroup(deliveryGroup);
        rbLBC.setSelected(true);
        
        rbLBC.setStyle("-fx-font-size: 11px;");
        rbJT.setStyle("-fx-font-size: 11px;");
        rbLalamove.setStyle("-fx-font-size: 11px;");
        rbOnsite.setStyle("-fx-font-size: 11px;");
        rbOthers.setStyle("-fx-font-size: 11px;");
        
        VBox deliveryBox = new VBox(5);
        deliveryBox.getChildren().addAll(rbLBC, rbJT, rbLalamove, rbOnsite, rbOthers);
        deliveryBox.setPadding(new Insets(0, 0, 0, 20));
        
        // Customer Information
        Label lblFullName = new Label("Full Name");
        lblFullName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtFullName = new TextField(Session.getCurrentUser() != null ? Session.getCurrentUser().getUsername() : "");
        txtFullName.setPromptText("Enter your full name");
        txtFullName.setStyle("-fx-font-size: 11px; -fx-border-color: #ddd; -fx-border-radius: 3;");
        
        Label lblContact = new Label("Contact Number");
        lblContact.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtContact = new TextField();
        txtContact.setPromptText("09XX XXX XXXX");
        txtContact.setStyle("-fx-font-size: 11px; -fx-border-color: #ddd; -fx-border-radius: 3;");
        
        Label lblAddress = new Label("Full Address");
        lblAddress.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtAddress = new TextField();
        txtAddress.setPromptText("Enter complete address");
        txtAddress.setStyle("-fx-font-size: 11px; -fx-border-color: #ddd; -fx-border-radius: 3;");
        
        Label lblFacebook = new Label("Facebook Link");
        lblFacebook.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtFacebook = new TextField();
        txtFacebook.setPromptText("https://facebook.com/...");
        txtFacebook.setStyle("-fx-font-size: 11px; -fx-border-color: #ddd; -fx-border-radius: 3;");
        
        // Terms and Conditions
        CheckBox chkTerms = new CheckBox("Signed Terms and Conditions");
        chkTerms.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
        
        // Upload Buttons
        String[] uploadedFiles = new String[3];
        
        Button btnUploadSelfie = new Button("⬇ Upload Selfie with Valid ID");
        btnUploadSelfie.setMaxWidth(Double.MAX_VALUE);
        btnUploadSelfie.setStyle("-fx-background-color: #f5c469; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
        btnUploadSelfie.setOnAction(e -> {
            String file = selectFile(dialog, "Select Selfie with Valid ID");
            if (file != null) {
                uploadedFiles[0] = file;
                btnUploadSelfie.setText("✓ Selfie Uploaded");
                btnUploadSelfie.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        });
        
        Button btnUploadID = new Button("⬇ Upload Valid ID");
        btnUploadID.setMaxWidth(Double.MAX_VALUE);
        btnUploadID.setStyle("-fx-background-color: #f5c469; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
        btnUploadID.setOnAction(e -> {
            String file = selectFile(dialog, "Select Valid ID");
            if (file != null) {
                uploadedFiles[1] = file;
                btnUploadID.setText("✓ Valid ID Uploaded");
                btnUploadID.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        });
        
        Button btnUploadProof = new Button("⬇ Upload Proof of Payment");
        btnUploadProof.setMaxWidth(Double.MAX_VALUE);
        btnUploadProof.setStyle("-fx-background-color: #f5c469; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
        btnUploadProof.setOnAction(e -> {
            String file = selectFile(dialog, "Select Proof of Payment");
            if (file != null) {
                uploadedFiles[2] = file;
                btnUploadProof.setText("✓ Proof of Payment Uploaded");
                btnUploadProof.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        });
        
        // Submit Button
        Button btnSubmit = new Button("Submit");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setStyle("-fx-background-color: #f5c469; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 20; -fx-cursor: hand;");
        btnSubmit.setAlignment(Pos.CENTER_RIGHT);
        
        btnSubmit.setOnAction(e -> {
            // Validate inputs
            if (txtFullName.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please enter your full name.");
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
            
            if (!chkTerms.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please accept the Terms and Conditions.");
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
            
            // Get selected delivery mode
            String deliveryMode = "LBC";
            if (rbJT.isSelected()) deliveryMode = "J&T";
            else if (rbLalamove.isSelected()) deliveryMode = "Lalamove";
            else if (rbOnsite.isSelected()) deliveryMode = "Onsite";
            else if (rbOthers.isSelected()) deliveryMode = "Others";
            
            // Create rental
            Rental rental = new Rental();
            rental.setCosplayId(cosplay.getId());
            rental.setCustomerName(txtFullName.getText().trim());
            rental.setContactNumber(txtContact.getText().trim());
            rental.setAddress(txtAddress.getText().trim());
            rental.setFacebookLink(txtFacebook.getText().trim());
            rental.setStartDate(start);
            rental.setEndDate(end);
            rental.setPaymentMethod(deliveryMode);
            rental.setProofOfPayment(uploadedFiles[2] != null ? uploadedFiles[2] : "");
            rental.setStatus("Pending");
            
            boolean success = rentalDAO.createRental(rental);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Your rental request has been submitted successfully!\n\n" +
                    "Rental ID: " + rental.getId() + "\n" +
                    "Status: Pending\n\n" +
                    "We will contact you shortly for confirmation.");
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create rental. The cosplay might have been booked by someone else.");
            }
        });
        
        // Add all components to content
        content.getChildren().addAll(
            titleLabel,
            lblCostumeSet, lblCostumeName,
            lblRentDays, daysBox,
            lblAddOns, txtAddOns,
            datesBox,
            lblDelivery, deliveryBox,
            lblFullName, txtFullName,
            lblContact, txtContact,
            lblAddress, txtAddress,
            lblFacebook, txtFacebook,
            chkTerms,
            btnUploadSelfie,
            btnUploadID,
            btnUploadProof,
            btnSubmit
        );
        
        // Wrapper for centering
        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20));
        wrapper.setStyle("-fx-background-color: #2c2c2c;");
        
        scrollPane.setContent(wrapper);
        Scene scene = new Scene(scrollPane, 500, 750);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private String selectFile(Stage owner, String title) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.pdf"),
            new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        java.io.File selectedFile = fileChooser.showOpenDialog(owner);
        return selectedFile != null ? selectedFile.getAbsolutePath() : null;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
