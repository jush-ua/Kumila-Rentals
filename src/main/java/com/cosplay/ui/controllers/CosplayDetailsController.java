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
            rental.setProofOfPayment("");
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
}
