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
import com.cosplay.util.StyledAlert;
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
    @FXML private TextField searchField;

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
        
        // Setup search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshCurrentSection());
        }
        
        showCatalog();
    }
    
    private void refreshCurrentSection() {
        switch (currentSection) {
            case "catalog":
                showCatalog();
                break;
            case "orders":
                showOrders();
                break;
            case "featured":
                showFeatured();
                break;
            case "events":
                showEventBanners();
                break;
        }
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
        var allCosplays = cosplayDAO.getAll();
        
        // Apply search filter
        String searchText = searchField != null ? searchField.getText() : null;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            allCosplays = allCosplays.stream()
                .filter(c -> c.getName().toLowerCase().contains(search) ||
                           (c.getSeriesName() != null && c.getSeriesName().toLowerCase().contains(search)) ||
                           (c.getCategory() != null && c.getCategory().toLowerCase().contains(search)))
                .collect(java.util.stream.Collectors.toList());
        }
        
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
        cosplayListView.setItems(FXCollections.observableArrayList(allCosplays));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private HBox createCosplayRow(Cosplay cosplay, int index) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 10, 15, 10));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label lblNo = new Label(String.valueOf(index));
        lblNo.setMinWidth(50);
        lblNo.setMaxWidth(50);
        lblNo.setStyle("-fx-font-size: 14px;");
        
        Label lblCategory = new Label(cosplay.getCategory() != null ? cosplay.getCategory() : "N/A");
        lblCategory.setMinWidth(150);
        lblCategory.setMaxWidth(150);
        lblCategory.setWrapText(true);
        lblCategory.setStyle("-fx-font-size: 14px;");
        
        Label lblSeries = new Label(cosplay.getSeriesName() != null ? cosplay.getSeriesName() : "N/A");
        lblSeries.setMinWidth(200);
        lblSeries.setPrefWidth(200);
        lblSeries.setMaxWidth(300);
        lblSeries.setWrapText(true);
        lblSeries.setStyle("-fx-font-size: 14px;");
        
        Label lblCharacter = new Label(cosplay.getName());
        lblCharacter.setMinWidth(200);
        lblCharacter.setPrefWidth(200);
        lblCharacter.setMaxWidth(300);
        lblCharacter.setWrapText(true);
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
        var allRentals = rentalDAO.getAllRentals();
        
        // Apply search filter
        String searchText = searchField != null ? searchField.getText() : null;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            allRentals = allRentals.stream()
                .filter(r -> r.getCustomerName().toLowerCase().contains(search) ||
                           r.getContactNumber().toLowerCase().contains(search) ||
                           (r.getStatus() != null && r.getStatus().toLowerCase().contains(search)))
                .collect(java.util.stream.Collectors.toList());
        }
        
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
        cosplayListView.setItems(FXCollections.observableArrayList(allRentals));
        cosplayListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }
    
    private void loadFeatured() {
        List<FeaturedItem> featured = featuredDAO.listAll();
        List<Cosplay> allCosplays = cosplayDAO.getAll();
        
        // Apply search filter
        String searchText = searchField != null ? searchField.getText() : null;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            allCosplays = allCosplays.stream()
                .filter(c -> c.getName().toLowerCase().contains(search) ||
                           (c.getSeriesName() != null && c.getSeriesName().toLowerCase().contains(search)))
                .collect(java.util.stream.Collectors.toList());
        }
        
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
        row.setPadding(new Insets(15, 10, 15, 10));
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label lblName = new Label(cosplay.getName());
        lblName.setMinWidth(250);
        lblName.setPrefWidth(250);
        lblName.setMaxWidth(350);
        lblName.setWrapText(true);
        lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblSeries = new Label(cosplay.getSeriesName() != null ? cosplay.getSeriesName() : "N/A");
        lblSeries.setMinWidth(200);
        lblSeries.setPrefWidth(200);
        lblSeries.setMaxWidth(300);
        lblSeries.setWrapText(true);
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
        // Delete the featured slot from database
        if (featuredDAO.delete(slot)) {
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
        Alert confirm = StyledAlert.createConfirmation(
            "Delete Cosplay",
            "Delete " + cosplay.getName() + "?",
            "This action cannot be undone."
        );
        
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
        scrollPane.setStyle("-fx-background-color: #F8F9FA;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox content = new VBox(18);
        content.setPadding(new Insets(0));
        content.setStyle("-fx-background-color: #F8F9FA;");
        
        // Header with gradient background
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 25, 25, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #F7A84C, #E89530); -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");
        Label title = new Label(cosplay == null ? "Add New Cosplay" : "Edit Cosplay Details");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 0 5;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, spacer, closeBtn);
        
        // Form container with white background
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 0;");
        
        Label lblCategory = new Label("Category");
        lblCategory.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtCategory = new TextField(cosplay != null ? cosplay.getCategory() : "");
        txtCategory.setPromptText("e.g., Anime, Game, Movie");
        txtCategory.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtCategory.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtCategory.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtCategory.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblSeries = new Label("Series Name");
        lblSeries.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtSeries = new TextField(cosplay != null ? cosplay.getSeriesName() : "");
        txtSeries.setPromptText("Series or franchise name");
        txtSeries.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtSeries.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtSeries.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtSeries.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblCharacter = new Label("Character Name *");
        lblCharacter.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtCharacter = new TextField(cosplay != null ? cosplay.getName() : "");
        txtCharacter.setPromptText("Character name (required)");
        txtCharacter.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtCharacter.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtCharacter.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtCharacter.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblSize = new Label("Size");
        lblSize.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtSize = new TextField(cosplay != null ? cosplay.getSize() : "");
        txtSize.setPromptText("e.g., S, M, L, XL");
        txtSize.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtSize.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtSize.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtSize.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblInclusions = new Label("Inclusions (one per line)");
        lblInclusions.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextArea txtInclusions = new TextArea(cosplay != null ? cosplay.getAddOns() : "");
        txtInclusions.setPromptText("Enter inclusions, one per line:\nComplete Costume Set\nWig\nAccessories\nProps");
        txtInclusions.setPrefRowCount(4);
        txtInclusions.setWrapText(true);
        txtInclusions.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtInclusions.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtInclusions.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtInclusions.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblRentRates = new Label("Rental Rates");
        lblRentRates.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        GridPane ratesGrid = new GridPane();
        ratesGrid.setHgap(12);
        ratesGrid.setVgap(10);
        
        Label lbl1Day = new Label("1 Day:");
        lbl1Day.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        TextField txt1Day = new TextField(cosplay != null && cosplay.getRentRate1Day() != null ? String.valueOf(cosplay.getRentRate1Day()) : "");
        txt1Day.setPromptText("0.00");
        txt1Day.setPrefWidth(150);
        txt1Day.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        
        Label lbl2Days = new Label("2 Days:");
        lbl2Days.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        TextField txt2Days = new TextField(cosplay != null && cosplay.getRentRate2Days() != null ? String.valueOf(cosplay.getRentRate2Days()) : "");
        txt2Days.setPromptText("0.00");
        txt2Days.setPrefWidth(150);
        txt2Days.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        
        Label lbl3Days = new Label("3 Days:");
        lbl3Days.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        TextField txt3Days = new TextField(cosplay != null && cosplay.getRentRate3Days() != null ? String.valueOf(cosplay.getRentRate3Days()) : "");
        txt3Days.setPromptText("0.00");
        txt3Days.setPrefWidth(150);
        txt3Days.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        
        ratesGrid.add(lbl1Day, 0, 0);
        ratesGrid.add(txt1Day, 1, 0);
        ratesGrid.add(lbl2Days, 0, 1);
        ratesGrid.add(txt2Days, 1, 1);
        ratesGrid.add(lbl3Days, 0, 2);
        ratesGrid.add(txt3Days, 1, 2);
        
        Label lblImagePath = new Label("Cosplay Image");
        lblImagePath.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        HBox imagePathBox = new HBox(0);
        imagePathBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtImagePath = new TextField(cosplay != null ? cosplay.getImagePath() : "");
        txtImagePath.setPromptText("Type path or browse: /com/cosplay/ui/images/cosplays/filename.png");
        txtImagePath.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8 0 0 8; -fx-background-radius: 8 0 0 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtImagePath.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtImagePath.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8 0 0 8; -fx-background-radius: 8 0 0 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtImagePath.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8 0 0 8; -fx-background-radius: 8 0 0 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        HBox.setHgrow(txtImagePath, Priority.ALWAYS);
        
        Button btnBrowse = new Button("Browse");
        btnBrowse.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 12 20; -fx-background-radius: 0 8 8 0; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;");
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
        
        Button btnSave = new Button(cosplay == null ? "Add Cosplay" : "Save Changes");
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);");
        btnSave.setOnMouseEntered(e -> btnSave.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 3);"));
        btnSave.setOnMouseExited(e -> btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);"));
        btnSave.setOnAction(e -> {
            String category = txtCategory.getText().trim();
            String series = txtSeries.getText().trim();
            String character = txtCharacter.getText().trim();
            String size = txtSize.getText().trim();
            String inclusions = txtInclusions.getText().trim();
            
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
                Cosplay newCosplay = new Cosplay(character, category, size, "", imagePath);
                newCosplay.setSeriesName(series);
                newCosplay.setRentRate1Day(rate1Day);
                newCosplay.setRentRate2Days(rate2Days);
                newCosplay.setRentRate3Days(rate3Days);
                newCosplay.setAddOns(inclusions);
                cosplayDAO.addCosplay(newCosplay);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cosplay added successfully!");
            } else {
                cosplay.setName(character);
                cosplay.setCategory(category);
                cosplay.setSeriesName(series);
                cosplay.setSize(size);
                cosplay.setDescription("");
                cosplay.setImagePath(imagePath);
                cosplay.setRentRate1Day(rate1Day);
                cosplay.setRentRate2Days(rate2Days);
                cosplay.setRentRate3Days(rate3Days);
                cosplay.setAddOns(inclusions);
                cosplayDAO.updateCosplay(cosplay);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cosplay updated successfully!");
            }
            
            loadCosplays();
            dialog.close();
        });
        
        HBox btnBox = new HBox(btnSave);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15, 0, 0, 0));
        
        formContainer.getChildren().addAll(lblCategory, txtCategory, lblSeries, txtSeries, 
                                     lblCharacter, txtCharacter, lblSize, txtSize,
                                     lblInclusions, txtInclusions,
                                     lblRentRates, ratesGrid, lblImagePath, imagePathBox, btnBox);
        
        content.getChildren().addAll(header, formContainer);
        
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 550, 650);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert;
        switch (type) {
            case ERROR:
                alert = StyledAlert.createError(title, message);
                break;
            case WARNING:
                alert = StyledAlert.createWarning(title, message);
                break;
            case CONFIRMATION:
                alert = StyledAlert.createConfirmation(title, null, message);
                break;
            default:
                alert = StyledAlert.createSuccess(title, message);
                break;
        }
        alert.show();
    }
    
    private void viewRentalDetails(Rental rental) {
        Stage dialog = new Stage();
        dialog.setTitle("Rental Details - Order #" + rental.getId());
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8F9FA;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #F8F9FA;");
        
        // Header with gradient
        VBox header = new VBox(5);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #F7A84C, #E89530); -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");
        Label title = new Label("Order Details");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label orderId = new Label("#" + rental.getId());
        orderId.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        header.getChildren().addAll(title, orderId);
        
        // Content card
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 0;");
        
        // Get cosplay details
        String cosplayDetails = "Cosplay ID: " + rental.getCosplayId();
        var cosplayOpt = cosplayDAO.findById(rental.getCosplayId());
        if (cosplayOpt.isPresent()) {
            Cosplay c = cosplayOpt.get();
            cosplayDetails = c.getName() + (c.getSeriesName() != null ? " (" + c.getSeriesName() + ")" : "");
        }
        
        // Cosplay Information Section
        VBox cosplaySection = new VBox(10);
        Label lblCosplayTitle = new Label("Cosplay Information");
        lblCosplayTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F7A84C;");
        
        GridPane cosplayGrid = new GridPane();
        cosplayGrid.setHgap(15);
        cosplayGrid.setVgap(10);
        int row = 0;
        addDetailRow(cosplayGrid, row++, "Cosplay:", cosplayDetails);
        addDetailRow(cosplayGrid, row++, "Rent Days:", rental.getRentDays() + " day(s)");
        
        cosplaySection.getChildren().addAll(lblCosplayTitle, cosplayGrid);
        
        // Rental Period Section
        VBox periodSection = new VBox(10);
        Label lblPeriodTitle = new Label("Rental Period");
        lblPeriodTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F7A84C;");
        
        GridPane periodGrid = new GridPane();
        periodGrid.setHgap(15);
        periodGrid.setVgap(10);
        row = 0;
        addDetailRow(periodGrid, row++, "Start Date:", rental.getStartDate().toString());
        addDetailRow(periodGrid, row++, "End Date:", rental.getEndDate().toString());
        addDetailRow(periodGrid, row++, "Status:", rental.getStatus() != null ? rental.getStatus() : "Pending");
        
        periodSection.getChildren().addAll(lblPeriodTitle, periodGrid);
        
        // Customer Information Section
        VBox customerSection = new VBox(10);
        Label lblCustomerTitle = new Label("Customer Information");
        lblCustomerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F7A84C;");
        
        GridPane customerGrid = new GridPane();
        customerGrid.setHgap(15);
        customerGrid.setVgap(10);
        row = 0;
        addDetailRow(customerGrid, row++, "Name:", rental.getCustomerName());
        addDetailRow(customerGrid, row++, "Contact Number:", rental.getContactNumber());
        addDetailRow(customerGrid, row++, "Address:", rental.getAddress());
        if (rental.getFacebookLink() != null && !rental.getFacebookLink().isBlank()) {
            addDetailRow(customerGrid, row++, "Facebook:", rental.getFacebookLink());
        }
        
        customerSection.getChildren().addAll(lblCustomerTitle, customerGrid);
        
        // Delivery & Additional Details Section
        VBox detailsSection = new VBox(10);
        Label lblDetailsTitle = new Label("Order Details");
        lblDetailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F7A84C;");
        
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(10);
        row = 0;
        addDetailRow(detailsGrid, row++, "Delivery Mode:", rental.getPaymentMethod());
        
        // Customer Add-ons
        if (rental.getCustomerAddOns() != null && !rental.getCustomerAddOns().isBlank()) {
            Label lblAddOnsKey = new Label("Customer Add-ons:");
            lblAddOnsKey.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #666;");
            
            TextArea txtAddOns = new TextArea(rental.getCustomerAddOns());
            txtAddOns.setEditable(false);
            txtAddOns.setWrapText(true);
            txtAddOns.setPrefRowCount(3);
            txtAddOns.setMaxWidth(350);
            txtAddOns.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0; -fx-border-radius: 4;");
            
            detailsGrid.add(lblAddOnsKey, 0, row);
            detailsGrid.add(txtAddOns, 1, row);
            row++;
        }
        
        detailsSection.getChildren().addAll(lblDetailsTitle, detailsGrid);
        
        // Uploaded Files Section
        VBox filesSection = new VBox(10);
        Label lblFilesTitle = new Label("Uploaded Documents");
        lblFilesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F7A84C;");
        
        GridPane filesGrid = new GridPane();
        filesGrid.setHgap(15);
        filesGrid.setVgap(10);
        row = 0;
        
        if (rental.getSelfiePhoto() != null && !rental.getSelfiePhoto().isBlank()) {
            addDetailRow(filesGrid, row++, "Selfie Photo:", rental.getSelfiePhoto());
        }
        if (rental.getIdPhoto() != null && !rental.getIdPhoto().isBlank()) {
            addDetailRow(filesGrid, row++, "Valid ID:", rental.getIdPhoto());
        }
        if (rental.getProofOfPayment() != null && !rental.getProofOfPayment().isBlank()) {
            addDetailRow(filesGrid, row++, "Proof of Payment:", rental.getProofOfPayment());
        }
        
        filesSection.getChildren().addAll(lblFilesTitle, filesGrid);
        
        // Add all sections to card
        card.getChildren().addAll(cosplaySection, new Separator(), periodSection, new Separator(), customerSection, new Separator(), detailsSection, new Separator(), filesSection);
        
        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-padding: 12 35; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #CACACA; -fx-text-fill: #333; -fx-padding: 12 35; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-padding: 12 35; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;"));
        btnClose.setOnAction(e -> dialog.close());
        
        HBox btnBox = new HBox(btnClose);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15, 0, 0, 0));
        card.getChildren().add(btnBox);
        
        content.getChildren().addAll(header, card);
        scrollPane.setContent(content);
        
        Scene scene = new Scene(scrollPane, 600, 700);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblKey = new Label(label);
        lblKey.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        lblValue.setWrapText(true);
        lblValue.setMaxWidth(350);
        
        grid.add(lblKey, 0, row);
        grid.add(lblValue, 1, row);
    }
    
    private void updateRentalStatus(Rental rental) {
        Stage dialog = new Stage();
        dialog.setTitle("Update Status - Order #" + rental.getId());
        
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #F8F9FA;");
        
        // Header
        VBox header = new VBox(5);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #F7A84C, #E89530); -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");
        Label title = new Label("Update Order Status");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitle = new Label("Order #" + rental.getId());
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        header.getChildren().addAll(title, subtitle);
        
        // Content card
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white;");
        
        Label lblCurrent = new Label("Current Status: " + (rental.getStatus() != null ? rental.getStatus() : "Pending"));
        lblCurrent.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-style: italic;");
        
        Label lblNew = new Label("Select New Status:");
        lblNew.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        ComboBox<String> cboStatus = new ComboBox<>();
        cboStatus.getItems().addAll("Pending", "Confirmed", "Rented", "Returned", "Cancelled");
        cboStatus.setValue(rental.getStatus() != null ? rental.getStatus() : "Pending");
        cboStatus.setPrefWidth(300);
        cboStatus.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button btnSave = new Button("Update Status");
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);");
        btnSave.setOnMouseEntered(e -> btnSave.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 3);"));
        btnSave.setOnMouseExited(e -> btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);"));
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
        btnCancel.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;");
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #CACACA; -fx-text-fill: #333; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;"));
        btnCancel.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(btnSave, btnCancel);
        
        card.getChildren().addAll(lblCurrent, lblNew, cboStatus, buttonBox);
        content.getChildren().addAll(header, card);
        
        Scene scene = new Scene(content, 480, 350);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML 
    public void goHome() { 
        SceneNavigator.navigate(Views.HOME); 
    }
    
    @FXML
    public void logout() {
        Session.clear();
        SceneNavigator.navigate(Views.LOGIN);
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
        scrollPane.setStyle("-fx-background-color: #F8F9FA;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #F8F9FA;");
        
        // Modern gradient header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #F7A84C, #E89530); -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");
        Label title = new Label(banner == null ? "Add Event Banner" : "Edit Event Banner");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 0 5;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, spacer, closeBtn);
        
        // Form container
        VBox formContainer = new VBox(18);
        formContainer.setPadding(new Insets(30));
        formContainer.setStyle("-fx-background-color: white;");
        
        Label lblTitle = new Label("Banner Title *");
        lblTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtTitle = new TextField(banner != null ? banner.getTitle() : "");
        txtTitle.setPromptText("Enter banner title");
        txtTitle.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtTitle.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtTitle.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtTitle.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblSubtitle = new Label("Subtitle");
        lblSubtitle.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtSubtitle = new TextField(banner != null ? banner.getSubtitle() : "");
        txtSubtitle.setPromptText("Enter subtitle (optional)");
        txtSubtitle.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtSubtitle.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtSubtitle.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtSubtitle.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblImage = new Label("Background Image");
        lblImage.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        HBox imageBox = new HBox(0);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        TextField txtImagePath = new TextField(banner != null ? banner.getImagePath() : "");
        txtImagePath.setPromptText("Select image file...");
        txtImagePath.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8 0 0 8; -fx-background-radius: 8 0 0 8; -fx-padding: 12 16; -fx-font-size: 14px; -fx-background-color: #F8F9FA;");
        txtImagePath.setEditable(false);
        HBox.setHgrow(txtImagePath, Priority.ALWAYS);
        Button btnBrowseImage = new Button("Browse");
        btnBrowseImage.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 12 20; -fx-background-radius: 0 8 8 0; -fx-cursor: hand; -fx-font-weight: 600; -fx-font-size: 14px;");
        btnBrowseImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Banner Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                txtImagePath.setText(selectedFile.getAbsolutePath());
            }
        });
        imageBox.getChildren().addAll(txtImagePath, btnBrowseImage);
        
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #E0E0E0;");
        
        Label lblEventDetails = new Label("Event Details (shown when clicked)");
        lblEventDetails.setStyle("-fx-font-weight: 600; -fx-font-size: 16px; -fx-text-fill: #F7A84C;");
        
        Label lblEventName = new Label("Event Name");
        lblEventName.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtEventName = new TextField(banner != null ? banner.getEventName() : "");
        txtEventName.setPromptText("Enter event name");
        txtEventName.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtEventName.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtEventName.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtEventName.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblVenue = new Label("Venue");
        lblVenue.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtVenue = new TextField(banner != null ? banner.getVenue() : "");
        txtVenue.setPromptText("Enter venue");
        txtVenue.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtVenue.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtVenue.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtVenue.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblOnsiteDate = new Label("Onsite Rent Date");
        lblOnsiteDate.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextField txtOnsiteDate = new TextField(banner != null ? banner.getOnsiteRentDate() : "");
        txtOnsiteDate.setPromptText("e.g., December 20-22, 2025");
        txtOnsiteDate.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtOnsiteDate.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtOnsiteDate.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtOnsiteDate.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #E0E0E0;");
        
        Label lblMessage = new Label("Banner Message (legacy - optional)");
        lblMessage.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        TextArea txtMessage = new TextArea(banner != null ? banner.getMessage() : "");
        txtMessage.setPromptText("Enter banner message");
        txtMessage.setPrefRowCount(2);
        txtMessage.setWrapText(true);
        txtMessage.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        txtMessage.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) txtMessage.setStyle("-fx-border-color: #F7A84C; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
            else txtMessage.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        });
        
        Label lblBgColor = new Label("Background Color (if no image)");
        lblBgColor.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        HBox bgColorBox = new HBox(10);
        bgColorBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtBgColor = new TextField(banner != null ? banner.getBackgroundColor() : "#fff4ed");
        txtBgColor.setPromptText("#fff4ed");
        txtBgColor.setPrefWidth(150);
        txtBgColor.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        
        ColorPicker bgColorPicker = new ColorPicker();
        bgColorPicker.setValue(parseColor(banner != null ? banner.getBackgroundColor() : "#fff4ed"));
        bgColorPicker.setStyle("-fx-pref-width: 80; -fx-pref-height: 42;");
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
        lblTextColor.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        HBox textColorBox = new HBox(10);
        textColorBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtTextColor = new TextField(banner != null ? banner.getTextColor() : "#d47f47");
        txtTextColor.setPromptText("#d47f47");
        txtTextColor.setPrefWidth(150);
        txtTextColor.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        
        ColorPicker textColorPicker = new ColorPicker();
        textColorPicker.setValue(parseColor(banner != null ? banner.getTextColor() : "#d47f47"));
        textColorPicker.setStyle("-fx-pref-width: 80; -fx-pref-height: 42;");
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
        chkActive.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-font-weight: 600;");
        
        formContainer.getChildren().addAll(lblTitle, txtTitle, lblSubtitle, txtSubtitle, 
            lblImage, imageBox, sep1, lblEventDetails, lblEventName, txtEventName, 
            lblVenue, txtVenue, lblOnsiteDate, txtOnsiteDate, sep2,
            lblMessage, txtMessage, lblBgColor, bgColorBox, lblTextColor, textColorBox, chkActive);
        
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: #F7A84C; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);");
        btnSave.setOnMouseEntered(e -> btnSave.setStyle("-fx-background-color: #E89530; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 3);"));
        btnSave.setOnMouseExited(e -> btnSave.setStyle("-fx-background-color: #F7A84C; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);"));
        btnSave.setOnAction(e -> {
            String bannerTitle = txtTitle.getText().trim();
            String bannerMessage = txtMessage.getText().trim();
            
            if (bannerTitle.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Banner title is required.");
                return;
            }
            
            if (banner == null) {
                EventBanner newBanner = new EventBanner();
                newBanner.setTitle(bannerTitle);
                newBanner.setMessage(bannerMessage.isEmpty() ? " " : bannerMessage);
                newBanner.setSubtitle(txtSubtitle.getText().trim());
                newBanner.setImagePath(txtImagePath.getText().trim());
                newBanner.setEventName(txtEventName.getText().trim());
                newBanner.setVenue(txtVenue.getText().trim());
                newBanner.setOnsiteRentDate(txtOnsiteDate.getText().trim());
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
                banner.setMessage(bannerMessage.isEmpty() ? " " : bannerMessage);
                banner.setSubtitle(txtSubtitle.getText().trim());
                banner.setImagePath(txtImagePath.getText().trim());
                banner.setEventName(txtEventName.getText().trim());
                banner.setVenue(txtVenue.getText().trim());
                banner.setOnsiteRentDate(txtOnsiteDate.getText().trim());
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
        btnCancel.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #CACACA; -fx-text-fill: #333; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-font-size: 15px; -fx-font-weight: 600; -fx-padding: 14 40; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnCancel.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(btnSave, btnCancel);
        formContainer.getChildren().add(buttonBox);
        
        content.getChildren().addAll(header, formContainer);
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 600, 800);
        scene.setFill(Color.TRANSPARENT);
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
