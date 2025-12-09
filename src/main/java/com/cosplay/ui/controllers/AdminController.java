package com.cosplay.ui.controllers;

import com.cosplay.dao.CosplayDAO;
import com.cosplay.dao.RentalDAO;
import com.cosplay.dao.FeaturedDAO;
import com.cosplay.dao.EventBannerDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.model.Rental;
import com.cosplay.model.FeaturedItem;
import com.cosplay.model.EventBanner;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class AdminController {
    @FXML private NavController navBarController;
    @FXML private ListView<Object> cosplayListView;
    @FXML private ListView<Rental> ordersListView;
    @FXML private HBox tableHeader;
    @FXML private Label lblSectionTitle;
    @FXML private Button btnAddRental;
    @FXML private Button btnCatalog;
    @FXML private Button btnOrders;
    @FXML private Button btnFeatured;
    @FXML private Button btnEvents;

    private final CosplayDAO cosplayDAO = new CosplayDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private final FeaturedDAO featuredDAO = new FeaturedDAO();
    private final EventBannerDAO eventBannerDAO = new EventBannerDAO();
    private String currentSection = "catalog";

    @FXML
    private void initialize() {
        if (navBarController != null) navBarController.setActive(Views.ADMIN);
        
        if (Session.getCurrentUser() == null || !"admin".equalsIgnoreCase(Session.getCurrentUser().getRole())) {
            SceneNavigator.navigate(Views.HOME);
            return;
        }
        
        showCatalog();
    }

    @FXML
    public void showCatalog() {
        currentSection = "catalog";
        lblSectionTitle.setText("Catalog");
        btnAddRental.setText("Add Rental");
        btnAddRental.setVisible(true);
        updateSidebarButtons();
        updateTableHeader();
        loadCosplays();
    }

    @FXML
    public void showOrders() {
        currentSection = "orders";
        lblSectionTitle.setText("Order Tracker");
        btnAddRental.setVisible(false);
        updateSidebarButtons();
        updateTableHeader();
        loadOrders();
    }
    
    @FXML
    public void showFeatured() {
        currentSection = "featured";
        lblSectionTitle.setText("Featured Cosplays");
        btnAddRental.setVisible(false);
        updateSidebarButtons();
        updateTableHeader();
        loadFeatured();
    }
    
    @FXML
    public void showEventBanners() {
        currentSection = "events";
        lblSectionTitle.setText("Event Banners");
        btnAddRental.setText("Add Event Banner");
        btnAddRental.setVisible(true);
        btnAddRental.setOnAction(e -> addEventBanner());
        updateSidebarButtons();
        updateTableHeader();
        loadEventBanners();
    }
    
    private void updateTableHeader() {
        if (tableHeader == null) return;
        
        tableHeader.getChildren().clear();
        
        String headerStyle = "-fx-font-weight: bold; -fx-font-size: 14px;";
        
        switch (currentSection) {
            case "catalog":
                addHeaderLabel("No.", 50);
                addHeaderLabel("Category", 150);
                addHeaderLabel("Series Name", 200);
                addHeaderLabel("Character Name", 200);
                addHeaderSpacer();
                break;
                
            case "orders":
                addHeaderLabel("Order Details", 450);
                addHeaderLabel("Status", 120);
                addHeaderLabel("Payment", 120);
                addHeaderSpacer();
                break;
                
            case "featured":
                addHeaderLabel("Cosplay Name", 250);
                addHeaderLabel("Series", 200);
                addHeaderLabel("Featured Status", 120);
                addHeaderSpacer();
                break;
                
            case "events":
                addHeaderLabel("Event Title", 300);
                addHeaderLabel("Status", 100);
                addHeaderLabel("Colors", 150);
                addHeaderSpacer();
                break;
        }
    }
    
    private void addHeaderLabel(String text, int minWidth) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        label.setMinWidth(minWidth);
        tableHeader.getChildren().add(label);
    }
    
    private void addHeaderSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        tableHeader.getChildren().add(spacer);
    }
    
    private void updateSidebarButtons() {
        String activeStyle = "-fx-background-color: #d4a574; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15; -fx-font-weight: bold; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15; -fx-cursor: hand;";
        
        btnCatalog.setStyle(inactiveStyle);
        btnOrders.setStyle(inactiveStyle);
        if (btnFeatured != null) btnFeatured.setStyle(inactiveStyle);
        if (btnEvents != null) btnEvents.setStyle(inactiveStyle);
        
        switch(currentSection) {
            case "catalog":
                btnCatalog.setStyle(activeStyle);
                break;
            case "orders":
                btnOrders.setStyle(activeStyle);
                break;
            case "featured":
                if (btnFeatured != null) btnFeatured.setStyle(activeStyle);
                break;
            case "events":
                if (btnEvents != null) btnEvents.setStyle(activeStyle);
                break;
        }
    }

    private void loadCosplays() {
        var cosplays = cosplayDAO.getAll();
        cosplayListView.setVisible(true);
        
        // Clear and reset the cell factory
        cosplayListView.setItems(null);
        cosplayListView.setCellFactory(lv -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item instanceof Cosplay) {
                    Cosplay cosplay = (Cosplay) item;
                    HBox row = createCosplayRow(cosplay, getIndex() + 1);
                    setGraphic(row);
                } else {
                    setGraphic(null);
                }
            }
        });
        
        // Set items after cell factory is configured
        cosplayListView.setItems(FXCollections.observableArrayList(cosplays));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private HBox createCosplayRow(Cosplay cosplay, int index) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label lblNo = new Label(String.valueOf(index));
        lblNo.setMinWidth(50);
        lblNo.setStyle("-fx-font-size: 14px;");
        
        Label lblCategory = new Label(cosplay.getCategory() != null ? cosplay.getCategory() : "N/A");
        lblCategory.setMinWidth(150);
        lblCategory.setStyle("-fx-font-size: 14px;");
        
        Label lblSeries = new Label(cosplay.getSeriesName() != null ? cosplay.getSeriesName() : "N/A");
        lblSeries.setMinWidth(200);
        lblSeries.setStyle("-fx-font-size: 14px;");
        
        Label lblCharacter = new Label(cosplay.getName());
        lblCharacter.setMinWidth(200);
        lblCharacter.setStyle("-fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnEdit = new Button("✏ Edit");
        btnEdit.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 15;");
        btnEdit.setOnAction(e -> editCosplay(cosplay));
        
        Button btnDelete = new Button("🗑 Delete");
        btnDelete.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 15;");
        btnDelete.setOnAction(e -> deleteCosplay(cosplay));
        
        row.getChildren().addAll(lblNo, lblCategory, lblSeries, lblCharacter, spacer, btnEdit, btnDelete);
        return row;
    }
    
    private HBox createRentalRow(Rental rental) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-background-color: white;");
        
        // Get cosplay details
        String cosplayName = "Unknown";
        var cosplayOpt = cosplayDAO.findById(rental.getCosplayId());
        if (cosplayOpt.isPresent()) {
            cosplayName = cosplayOpt.get().getName();
        }
        
        // Order Details column - consolidated (450px to match header)
        VBox detailsBox = new VBox(5);
        detailsBox.setMinWidth(450);
        
        Label lblOrder = new Label("Order #" + rental.getId());
        lblOrder.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e6a84c;");
        
        Label lblCosplay = new Label("Cosplay: " + cosplayName);
        lblCosplay.setStyle("-fx-font-size: 13px;");
        
        Label lblCustomer = new Label("Customer: " + rental.getCustomerName());
        lblCustomer.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label lblContact = new Label("Contact: " + rental.getContactNumber());
        lblContact.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label lblDates = new Label("Period: " + rental.getStartDate() + " to " + rental.getEndDate());
        lblDates.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        detailsBox.getChildren().addAll(lblOrder, lblCosplay, lblCustomer, lblContact, lblDates);
        
        // Status column (120px to match header)
        VBox statusBox = new VBox(3);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(120);
        
        Label lblStatusLabel = new Label("Status:");
        lblStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        Label lblStatusValue = new Label(rental.getStatus() != null ? rental.getStatus() : "Pending");
        String statusColor = switch(rental.getStatus() != null ? rental.getStatus() : "Pending") {
            case "Confirmed" -> "#4CAF50";
            case "Rented" -> "#2196F3";
            case "Returned" -> "#9E9E9E";
            case "Cancelled" -> "#F44336";
            default -> "#FF9800";
        };
        lblStatusValue.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + statusColor + ";");
        
        statusBox.getChildren().addAll(lblStatusLabel, lblStatusValue);
        
        // Payment column (120px to match header)
        VBox paymentBox = new VBox(3);
        paymentBox.setAlignment(Pos.CENTER_LEFT);
        paymentBox.setMinWidth(120);
        
        Label lblPaymentLabel = new Label("Payment:");
        lblPaymentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        Label lblPaymentValue = new Label(rental.getPaymentMethod() != null ? rental.getPaymentMethod() : "N/A");
        lblPaymentValue.setStyle("-fx-font-size: 13px;");
        
        paymentBox.getChildren().addAll(lblPaymentLabel, lblPaymentValue);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox actionsBox = new VBox(5);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnView = new Button("👁 View");
        btnView.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 15; -fx-font-size: 12px;");
        btnView.setOnAction(e -> viewRentalDetails(rental));
        
        Button btnUpdateStatus = new Button("✓ Update Status");
        btnUpdateStatus.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 15; -fx-font-size: 12px;");
        btnUpdateStatus.setOnAction(e -> updateRentalStatus(rental));
        
        actionsBox.getChildren().addAll(btnView, btnUpdateStatus);
        
        row.getChildren().addAll(detailsBox, statusBox, paymentBox, spacer, actionsBox);
        return row;
    }
    
    private void loadOrders() {
        var rentals = rentalDAO.getAllRentals();
        cosplayListView.setVisible(true);
        
        // Clear and reset the cell factory
        cosplayListView.setItems(null);
        cosplayListView.setCellFactory(lv -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item instanceof Rental) {
                    Rental rental = (Rental) item;
                    HBox row = createRentalRow(rental);
                    setGraphic(row);
                } else {
                    setGraphic(null);
                }
            }
        });
        
        // Set items after cell factory is configured
        cosplayListView.setItems(FXCollections.observableArrayList(rentals));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private void loadFeatured() {
        List<FeaturedItem> featured = featuredDAO.listAll();
        List<Cosplay> allCosplays = cosplayDAO.getAll();
        
        cosplayListView.setVisible(true);
        cosplayListView.setItems(null);
        
        cosplayListView.setCellFactory(lv -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item instanceof Cosplay) {
                    Cosplay cosplay = (Cosplay) item;
                    HBox row = createFeaturedRow(cosplay, featured);
                    setGraphic(row);
                } else {
                    setGraphic(null);
                }
            }
        });
        
        cosplayListView.setItems(FXCollections.observableArrayList(allCosplays));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private HBox createFeaturedRow(Cosplay cosplay, List<FeaturedItem> featured) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label lblName = new Label(cosplay.getName());
        lblName.setMinWidth(250);
        lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblSeries = new Label(cosplay.getSeriesName() != null ? cosplay.getSeriesName() : "N/A");
        lblSeries.setMinWidth(200);
        lblSeries.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        
        // Check which slots this cosplay is featured in
        VBox slotsBox = new VBox(3);
        slotsBox.setMinWidth(120);
        boolean isFeatured = false;
        StringBuilder slotText = new StringBuilder();
        for (FeaturedItem fi : featured) {
            if (fi.getCosplayId() != null && fi.getCosplayId() == cosplay.getId()) {
                isFeatured = true;
                if (slotText.length() > 0) slotText.append(", ");
                slotText.append("Slot ").append(fi.getSlot());
            }
        }
        
        if (isFeatured) {
            Label lblStatus = new Label("⭐ Featured");
            lblStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
            Label lblSlots = new Label(slotText.toString());
            lblSlots.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
            slotsBox.getChildren().addAll(lblStatus, lblSlots);
        } else {
            Label lblStatus = new Label("Not Featured");
            lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            slotsBox.getChildren().add(lblStatus);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Add buttons to set featured in each slot
        for (int slot = 1; slot <= 4; slot++) {
            final int slotNum = slot;
            boolean isInSlot = featured.stream()
                .anyMatch(fi -> fi.getSlot() == slotNum && fi.getCosplayId() != null && fi.getCosplayId() == cosplay.getId());
            
            Button btnSlot = new Button(isInSlot ? "✓ Slot " + slot : "Set Slot " + slot);
            btnSlot.setStyle(isInSlot 
                ? "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 11px;"
                : "-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 11px;");
            
            btnSlot.setOnAction(e -> {
                if (isInSlot) {
                    removeFeatured(slotNum);
                } else {
                    setFeatured(cosplay, slotNum);
                }
            });
            
            buttonsBox.getChildren().add(btnSlot);
        }
        
        row.getChildren().addAll(lblName, lblSeries, slotsBox, spacer, buttonsBox);
        return row;
    }
    
    private void setFeatured(Cosplay cosplay, int slot) {
        FeaturedItem item = new FeaturedItem();
        item.setSlot(slot);
        item.setCosplayId(cosplay.getId());
        item.setTitle(cosplay.getName());
        item.setImageUrl(cosplay.getImagePath());
        
        if (featuredDAO.save(item)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(cosplay.getName() + " has been set as featured in Slot " + slot);
            alert.showAndWait();
            loadFeatured();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to set featured cosplay");
            alert.showAndWait();
        }
    }
    
    private void removeFeatured(int slot) {
        // Save with null cosplayId to clear the slot
        FeaturedItem item = new FeaturedItem();
        item.setSlot(slot);
        item.setCosplayId(null);
        item.setTitle("");
        item.setImageUrl("");
        
        if (featuredDAO.save(item)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Slot " + slot + " has been cleared");
            alert.showAndWait();
            loadFeatured();
        }
    }

    @FXML
    public void addCosplay() {
        showCosplayDialog(null);
    }
    
    private void editCosplay(Cosplay cosplay) {
        showCosplayDialog(cosplay);
    }
    
    private void deleteCosplay(Cosplay cosplay) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Cosplay");
        confirm.setHeaderText("Delete " + cosplay.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = cosplayDAO.deleteCosplay(cosplay.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Cosplay deleted successfully!");
                    loadCosplays();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete cosplay.");
                }
            }
        });
    }
    
    private void showCosplayDialog(Cosplay cosplay) {
        Stage dialog = new Stage();
        dialog.setTitle(cosplay == null ? "Add Rental" : "Edit Rental");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(cosplay == null ? "Set Details" : "Edit Details");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e6a84c; -fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, spacer, closeBtn);
        
        Label lblCategory = new Label("Category");
        lblCategory.setStyle("-fx-font-weight: bold;");
        TextField txtCategory = new TextField(cosplay != null ? cosplay.getCategory() : "");
        txtCategory.setPromptText("Category");
        txtCategory.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblSeries = new Label("Series Name");
        lblSeries.setStyle("-fx-font-weight: bold;");
        TextField txtSeries = new TextField(cosplay != null ? cosplay.getSeriesName() : "");
        txtSeries.setPromptText("Series Name");
        txtSeries.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblCharacter = new Label("Character Name");
        lblCharacter.setStyle("-fx-font-weight: bold;");
        TextField txtCharacter = new TextField(cosplay != null ? cosplay.getName() : "");
        txtCharacter.setPromptText("Character Name");
        txtCharacter.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblSize = new Label("Size");
        lblSize.setStyle("-fx-font-weight: bold;");
        TextField txtSize = new TextField(cosplay != null ? cosplay.getSize() : "");
        txtSize.setPromptText("Size");
        txtSize.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblDescription = new Label("Description");
        lblDescription.setStyle("-fx-font-weight: bold;");
        TextArea txtDescription = new TextArea(cosplay != null ? cosplay.getDescription() : "");
        txtDescription.setPromptText("Enter a detailed description of the cosplay...");
        txtDescription.setPrefRowCount(4);
        txtDescription.setWrapText(true);
        txtDescription.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblRentRates = new Label("Rent Rates");
        lblRentRates.setStyle("-fx-font-weight: bold;");
        
        GridPane ratesGrid = new GridPane();
        ratesGrid.setHgap(10);
        ratesGrid.setVgap(8);
        
        Label lbl1Day = new Label("1 day");
        TextField txt1Day = new TextField(cosplay != null && cosplay.getRentRate1Day() != null ? String.valueOf(cosplay.getRentRate1Day()) : "");
        txt1Day.setPromptText("0.00");
        txt1Day.setPrefWidth(120);
        txt1Day.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lbl2Days = new Label("2 days");
        TextField txt2Days = new TextField(cosplay != null && cosplay.getRentRate2Days() != null ? String.valueOf(cosplay.getRentRate2Days()) : "");
        txt2Days.setPromptText("0.00");
        txt2Days.setPrefWidth(120);
        txt2Days.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lbl3Days = new Label("3 days");
        TextField txt3Days = new TextField(cosplay != null && cosplay.getRentRate3Days() != null ? String.valueOf(cosplay.getRentRate3Days()) : "");
        txt3Days.setPromptText("0.00");
        txt3Days.setPrefWidth(120);
        txt3Days.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        ratesGrid.add(lbl1Day, 0, 0);
        ratesGrid.add(txt1Day, 1, 0);
        ratesGrid.add(lbl2Days, 0, 1);
        ratesGrid.add(txt2Days, 1, 1);
        ratesGrid.add(lbl3Days, 0, 2);
        ratesGrid.add(txt3Days, 1, 2);
        
        Label lblImagePath = new Label("Image Path/URL");
        lblImagePath.setStyle("-fx-font-weight: bold;");
        
        HBox imagePathBox = new HBox(10);
        imagePathBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtImagePath = new TextField(cosplay != null ? cosplay.getImagePath() : "");
        txtImagePath.setPromptText("Enter image file path or URL");
        txtImagePath.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        HBox.setHgrow(txtImagePath, Priority.ALWAYS);
        
        Button btnBrowse = new Button("Browse...");
        btnBrowse.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        btnBrowse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cosplay Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            // Set initial directory to user's Pictures folder if it exists
            File picturesDir = new File(System.getProperty("user.home"), "Pictures");
            if (picturesDir.exists()) {
                fileChooser.setInitialDirectory(picturesDir);
            }
            
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                txtImagePath.setText(selectedFile.getAbsolutePath());
            }
        });
        
        imagePathBox.getChildren().addAll(txtImagePath, btnBrowse);
        
        Label lblAddOns = new Label("Add Ons");
        lblAddOns.setStyle("-fx-font-weight: bold;");
        TextArea txtAddOns = new TextArea(cosplay != null ? cosplay.getAddOns() : "");
        txtAddOns.setPromptText("Enter add-ons (one per line)");
        txtAddOns.setPrefRowCount(3);
        txtAddOns.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 20; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            String category = txtCategory.getText().trim();
            String series = txtSeries.getText().trim();
            String character = txtCharacter.getText().trim();
            String size = txtSize.getText().trim();
            String description = txtDescription.getText().trim();
            
            if (character.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Character name is required.");
                return;
            }
            
            Double rate1Day = null;
            Double rate2Days = null;
            Double rate3Days = null;
            
            try {
                if (!txt1Day.getText().trim().isEmpty()) {
                    rate1Day = Double.parseDouble(txt1Day.getText().trim());
                }
                if (!txt2Days.getText().trim().isEmpty()) {
                    rate2Days = Double.parseDouble(txt2Days.getText().trim());
                }
                if (!txt3Days.getText().trim().isEmpty()) {
                    rate3Days = Double.parseDouble(txt3Days.getText().trim());
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Please enter valid numbers for rent rates.");
                return;
            }
            
            String imagePath = txtImagePath.getText().trim();
            
            if (cosplay == null) {
                Cosplay newCosplay = new Cosplay(character, category, size, description, imagePath);
                newCosplay.setSeriesName(series);
                newCosplay.setRentRate1Day(rate1Day);
                newCosplay.setRentRate2Days(rate2Days);
                newCosplay.setRentRate3Days(rate3Days);
                newCosplay.setAddOns(txtAddOns.getText().trim());
                cosplayDAO.addCosplay(newCosplay);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cosplay added successfully!");
            } else {
                cosplay.setName(character);
                cosplay.setCategory(category);
                cosplay.setSeriesName(series);
                cosplay.setSize(size);
                cosplay.setDescription(description);
                cosplay.setImagePath(imagePath);
                cosplay.setRentRate1Day(rate1Day);
                cosplay.setRentRate2Days(rate2Days);
                cosplay.setRentRate3Days(rate3Days);
                cosplay.setAddOns(txtAddOns.getText().trim());
                cosplayDAO.updateCosplay(cosplay);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cosplay updated successfully!");
            }
            
            loadCosplays();
            dialog.close();
        });
        
        HBox btnBox = new HBox(btnSave);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));
        
        content.getChildren().addAll(header, lblCategory, txtCategory, lblSeries, txtSeries, 
                                     lblCharacter, txtCharacter, lblSize, txtSize,
                                     lblDescription, txtDescription,
                                     lblRentRates, ratesGrid, lblImagePath, imagePathBox, 
                                     lblAddOns, txtAddOns, btnBox);
        
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 500, 700);
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
    
    private void viewRentalDetails(Rental rental) {
        Stage dialog = new Stage();
        dialog.setTitle("Rental Details - Order #" + rental.getId());
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        
        Label title = new Label("Order #" + rental.getId());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        
        // Get cosplay details
        String cosplayDetails = "Cosplay ID: " + rental.getCosplayId();
        var cosplayOpt = cosplayDAO.findById(rental.getCosplayId());
        if (cosplayOpt.isPresent()) {
            Cosplay c = cosplayOpt.get();
            cosplayDetails = c.getName() + (c.getSeriesName() != null ? " (" + c.getSeriesName() + ")" : "");
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        addDetailRow(grid, row++, "Cosplay:", cosplayDetails);
        addDetailRow(grid, row++, "Customer Name:", rental.getCustomerName());
        addDetailRow(grid, row++, "Contact Number:", rental.getContactNumber());
        addDetailRow(grid, row++, "Address:", rental.getAddress());
        if (rental.getFacebookLink() != null && !rental.getFacebookLink().isBlank()) {
            addDetailRow(grid, row++, "Facebook:", rental.getFacebookLink());
        }
        addDetailRow(grid, row++, "Start Date:", rental.getStartDate().toString());
        addDetailRow(grid, row++, "End Date:", rental.getEndDate().toString());
        addDetailRow(grid, row++, "Payment Method:", rental.getPaymentMethod());
        addDetailRow(grid, row++, "Status:", rental.getStatus() != null ? rental.getStatus() : "Pending");
        
        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-padding: 10 30; -fx-background-radius: 20; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());
        
        HBox btnBox = new HBox(btnClose);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));
        
        content.getChildren().addAll(title, new Separator(), grid, btnBox);
        scrollPane.setContent(content);
        
        Scene scene = new Scene(scrollPane, 500, 450);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblKey = new Label(label);
        lblKey.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.setStyle("-fx-font-size: 13px;");
        lblValue.setWrapText(true);
        lblValue.setMaxWidth(300);
        
        grid.add(lblKey, 0, row);
        grid.add(lblValue, 1, row);
    }
    
    private void updateRentalStatus(Rental rental) {
        Stage dialog = new Stage();
        dialog.setTitle("Update Status - Order #" + rental.getId());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        
        Label title = new Label("Update Order Status");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        
        Label lblCurrent = new Label("Current Status: " + (rental.getStatus() != null ? rental.getStatus() : "Pending"));
        lblCurrent.setStyle("-fx-font-size: 13px;");
        
        Label lblNew = new Label("New Status:");
        lblNew.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        ComboBox<String> cboStatus = new ComboBox<>();
        cboStatus.getItems().addAll("Pending", "Confirmed", "Rented", "Returned", "Cancelled");
        cboStatus.setValue(rental.getStatus() != null ? rental.getStatus() : "Pending");
        cboStatus.setPrefWidth(250);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-padding: 10 30; -fx-background-radius: 20; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            rental.setStatus(cboStatus.getValue());
            boolean success = rentalDAO.updateRentalStatus(rental.getId(), cboStatus.getValue());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Rental status updated successfully!");
                loadOrders();
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update rental status.");
            }
        });
        
        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ccc; -fx-text-fill: #333; -fx-padding: 10 30; -fx-background-radius: 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(btnSave, btnCancel);
        
        content.getChildren().addAll(title, new Separator(), lblCurrent, lblNew, cboStatus, buttonBox);
        
        Scene scene = new Scene(content, 400, 280);
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML 
    public void goHome() { 
        SceneNavigator.navigate(Views.HOME); 
    }
    
    // ===========================
    // Event Banner Management
    // ===========================
    
    private void loadEventBanners() {
        List<EventBanner> banners = eventBannerDAO.getAll();
        
        cosplayListView.setVisible(true);
        cosplayListView.setItems(null);
        
        cosplayListView.setCellFactory(lv -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item instanceof EventBanner) {
                    EventBanner banner = (EventBanner) item;
                    HBox row = createEventBannerRow(banner);
                    setGraphic(row);
                } else {
                    setGraphic(null);
                }
            }
        });
        
        cosplayListView.setItems(FXCollections.observableArrayList(banners));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private HBox createEventBannerRow(EventBanner banner) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-background-color: " + 
                     (banner.isActive() ? "#f0f8ff" : "white") + ";");
        
        VBox detailsBox = new VBox(5);
        detailsBox.setMinWidth(300);
        
        Label lblTitle = new Label(banner.getTitle());
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label lblMessage = new Label(banner.getMessage());
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(280);
        lblMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        detailsBox.getChildren().addAll(lblTitle, lblMessage);
        
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(100);
        
        Label lblStatus = new Label(banner.isActive() ? "✓ Active" : "Inactive");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + 
                          (banner.isActive() ? "#4CAF50" : "#999") + ";");
        
        statusBox.getChildren().add(lblStatus);
        
        VBox colorBox = new VBox(5);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        colorBox.setMinWidth(150);
        
        HBox colorPreview = new HBox(10);
        colorPreview.setAlignment(Pos.CENTER_LEFT);
        
        Region bgColorSquare = new Region();
        bgColorSquare.setPrefSize(30, 30);
        bgColorSquare.setStyle("-fx-background-color: " + banner.getBackgroundColor() + "; -fx-border-color: #ccc;");
        
        Region txtColorSquare = new Region();
        txtColorSquare.setPrefSize(30, 30);
        txtColorSquare.setStyle("-fx-background-color: " + banner.getTextColor() + "; -fx-border-color: #ccc;");
        
        colorPreview.getChildren().addAll(bgColorSquare, txtColorSquare);
        
        Label lblColors = new Label("BG / Text");
        lblColors.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        
        colorBox.getChildren().addAll(colorPreview, lblColors);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnToggleActive = new Button(banner.isActive() ? "Deactivate" : "Activate");
        btnToggleActive.setStyle("-fx-background-color: " + (banner.isActive() ? "#FF9800" : "#4CAF50") + 
                                "; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12; -fx-font-size: 11px;");
        btnToggleActive.setOnAction(e -> toggleEventBannerActive(banner));
        
        Button btnEdit = new Button("✏ Edit");
        btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12; -fx-font-size: 11px;");
        btnEdit.setOnAction(e -> editEventBanner(banner));
        
        Button btnDelete = new Button("🗑 Delete");
        btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12; -fx-font-size: 11px;");
        btnDelete.setOnAction(e -> deleteEventBanner(banner));
        
        buttonsBox.getChildren().addAll(btnToggleActive, btnEdit, btnDelete);
        
        row.getChildren().addAll(detailsBox, statusBox, colorBox, spacer, buttonsBox);
        return row;
    }
    
    private void addEventBanner() {
        showEventBannerDialog(null);
    }
    
    private void editEventBanner(EventBanner banner) {
        showEventBannerDialog(banner);
    }
    
    private void deleteEventBanner(EventBanner banner) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event Banner");
        confirm.setHeaderText("Delete \"" + banner.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (eventBannerDAO.delete(banner.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event banner deleted successfully!");
                    loadEventBanners();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event banner.");
                }
            }
        });
    }
    
    private void toggleEventBannerActive(EventBanner banner) {
        if (eventBannerDAO.toggleActive(banner.getId())) {
            loadEventBanners();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update banner status.");
        }
    }
    
    private void showEventBannerDialog(EventBanner banner) {
        Stage dialog = new Stage();
        dialog.setTitle(banner == null ? "Add Event Banner" : "Edit Event Banner");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(banner == null ? "New Event Banner" : "Edit Event Banner");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e6a84c;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e6a84c; -fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, spacer, closeBtn);
        
        Label lblTitle = new Label("Banner Title");
        lblTitle.setStyle("-fx-font-weight: bold;");
        TextField txtTitle = new TextField(banner != null ? banner.getTitle() : "");
        txtTitle.setPromptText("Enter banner title");
        txtTitle.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblMessage = new Label("Banner Message");
        lblMessage.setStyle("-fx-font-weight: bold;");
        TextArea txtMessage = new TextArea(banner != null ? banner.getMessage() : "");
        txtMessage.setPromptText("Enter banner message");
        txtMessage.setPrefRowCount(3);
        txtMessage.setWrapText(true);
        txtMessage.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        Label lblBgColor = new Label("Background Color");
        lblBgColor.setStyle("-fx-font-weight: bold;");
        
        HBox bgColorBox = new HBox(10);
        bgColorBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtBgColor = new TextField(banner != null ? banner.getBackgroundColor() : "#fff4ed");
        txtBgColor.setPromptText("#fff4ed");
        txtBgColor.setPrefWidth(150);
        txtBgColor.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        ColorPicker bgColorPicker = new ColorPicker();
        bgColorPicker.setValue(parseColor(banner != null ? banner.getBackgroundColor() : "#fff4ed"));
        bgColorPicker.setOnAction(e -> {
            Color color = bgColorPicker.getValue();
            txtBgColor.setText(toHexString(color));
        });
        
        txtBgColor.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Color color = parseColor(newVal);
                bgColorPicker.setValue(color);
            } catch (Exception ignored) {}
        });
        
        bgColorBox.getChildren().addAll(txtBgColor, bgColorPicker);
        
        Label lblTextColor = new Label("Text Color");
        lblTextColor.setStyle("-fx-font-weight: bold;");
        
        HBox textColorBox = new HBox(10);
        textColorBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtTextColor = new TextField(banner != null ? banner.getTextColor() : "#d47f47");
        txtTextColor.setPromptText("#d47f47");
        txtTextColor.setPrefWidth(150);
        txtTextColor.setStyle("-fx-border-color: #e6a84c; -fx-border-radius: 5; -fx-padding: 8;");
        
        ColorPicker textColorPicker = new ColorPicker();
        textColorPicker.setValue(parseColor(banner != null ? banner.getTextColor() : "#d47f47"));
        textColorPicker.setOnAction(e -> {
            Color color = textColorPicker.getValue();
            txtTextColor.setText(toHexString(color));
        });
        
        txtTextColor.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Color color = parseColor(newVal);
                textColorPicker.setValue(color);
            } catch (Exception ignored) {}
        });
        
        textColorBox.getChildren().addAll(txtTextColor, textColorPicker);
        
        CheckBox chkActive = new CheckBox("Set as Active Banner");
        chkActive.setSelected(banner != null && banner.isActive());
        chkActive.setStyle("-fx-font-size: 13px;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: #e6a84c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 20; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            String bannerTitle = txtTitle.getText().trim();
            String bannerMessage = txtMessage.getText().trim();
            
            if (bannerTitle.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Banner title is required.");
                return;
            }
            
            if (bannerMessage.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Banner message is required.");
                return;
            }
            
            if (banner == null) {
                EventBanner newBanner = new EventBanner();
                newBanner.setTitle(bannerTitle);
                newBanner.setMessage(bannerMessage);
                newBanner.setBackgroundColor(txtBgColor.getText().trim());
                newBanner.setTextColor(txtTextColor.getText().trim());
                newBanner.setActive(chkActive.isSelected());
                
                if (eventBannerDAO.save(newBanner)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event banner created successfully!");
                    loadEventBanners();
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event banner.");
                }
            } else {
                banner.setTitle(bannerTitle);
                banner.setMessage(bannerMessage);
                banner.setBackgroundColor(txtBgColor.getText().trim());
                banner.setTextColor(txtTextColor.getText().trim());
                banner.setActive(chkActive.isSelected());
                
                if (eventBannerDAO.save(banner)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event banner updated successfully!");
                    loadEventBanners();
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event banner.");
                }
            }
        });
        
        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ccc; -fx-text-fill: #333; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(btnSave, btnCancel);
        
        content.getChildren().addAll(
            header,
            new Separator(),
            lblTitle, txtTitle,
            lblMessage, txtMessage,
            lblBgColor, bgColorBox,
            lblTextColor, textColorBox,
            chkActive,
            buttonBox
        );
        
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 550, 600);
        dialog.setScene(scene);
        dialog.show();
    }
    
    /**
     * Parse a hex color string to JavaFX Color
     */
    private Color parseColor(String hexColor) {
        try {
            if (hexColor == null || hexColor.isEmpty()) {
                return Color.web("#fff4ed");
            }
            if (!hexColor.startsWith("#")) {
                hexColor = "#" + hexColor;
            }
            return Color.web(hexColor);
        } catch (Exception e) {
            return Color.web("#fff4ed");
        }
    }
    
    /**
     * Convert JavaFX Color to hex string
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
}
