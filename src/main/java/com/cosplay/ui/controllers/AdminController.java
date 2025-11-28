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
import javafx.collections.FXCollections;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class AdminController {
    @FXML private NavController navBarController;
    @FXML private ListView<Cosplay> cosplayListView;
    @FXML private ListView<Rental> ordersListView;
    @FXML private Label lblSectionTitle;
    @FXML private Button btnAddRental;
    @FXML private Button btnCatalog;
    @FXML private Button btnOrders;

    private final CosplayDAO cosplayDAO = new CosplayDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
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
        loadCosplays();
    }

    @FXML
    public void showOrders() {
        currentSection = "orders";
        lblSectionTitle.setText("Order Tracker");
        btnAddRental.setVisible(false);
        updateSidebarButtons();
        loadOrders();
    }
    
    private void updateSidebarButtons() {
        String activeStyle = "-fx-background-color: #d4a574; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15; -fx-font-weight: bold; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15; -fx-cursor: hand;";
        
        if ("catalog".equals(currentSection)) {
            btnCatalog.setStyle(activeStyle);
            btnOrders.setStyle(inactiveStyle);
        } else {
            btnCatalog.setStyle(inactiveStyle);
            btnOrders.setStyle(activeStyle);
        }
    }

    private void loadCosplays() {
        var cosplays = cosplayDAO.getAll();
        cosplayListView.setItems(FXCollections.observableArrayList(cosplays));
        
        cosplayListView.setCellFactory(lv -> new ListCell<Cosplay>() {
            @Override
            protected void updateItem(Cosplay cosplay, boolean empty) {
                super.updateItem(cosplay, empty);
                if (empty || cosplay == null) {
                    setGraphic(null);
                } else {
                    HBox row = createCosplayRow(cosplay, getIndex() + 1);
                    setGraphic(row);
                }
            }
        });
        
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
    
    private void loadOrders() {
        var rentals = rentalDAO.getAllRentals();
        cosplayListView.setItems(null);
        ListView<String> tempView = new ListView<>();
        tempView.setItems(FXCollections.observableArrayList(
            rentals.stream()
                .map(r -> "Rental #" + r.getId() + " - Cosplay ID: " + r.getCosplayId() + " - Status: " + r.getStatus())
                .toList()
        ));
        cosplayListView.setVisible(false);
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
            
            if (cosplay == null) {
                Cosplay newCosplay = new Cosplay(character, category, size, "", "");
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
                                     lblRentRates, ratesGrid, lblAddOns, txtAddOns, btnBox);
        
        scrollPane.setContent(content);
        Scene scene = new Scene(scrollPane, 500, 650);
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

    @FXML 
    public void goHome() { 
        SceneNavigator.navigate(Views.HOME); 
    }
}
