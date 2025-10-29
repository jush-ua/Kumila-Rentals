package com.cosplay.ui.controllers;

import com.cosplay.dao.FeaturedDAO;
import com.cosplay.dao.CostumeDAO;
import com.cosplay.model.FeaturedItem;
import com.cosplay.model.Costume;
import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;

public class AdminController {
    @FXML private NavController navBarController;

    @FXML private ComboBox<Costume> cbo1; @FXML private TextField title1;
    @FXML private ComboBox<Costume> cbo2; @FXML private TextField title2;
    @FXML private ComboBox<Costume> cbo3; @FXML private TextField title3;
    @FXML private ComboBox<Costume> cbo4; @FXML private TextField title4;

    private final FeaturedDAO dao = new FeaturedDAO();
    private final CostumeDAO costumeDAO = new CostumeDAO();

    @FXML
    private void initialize() {
        if (navBarController != null) navBarController.setActive(Views.ADMIN);
        // Guard: Only admins should be here
        if (Session.getCurrentUser() == null || !"admin".equalsIgnoreCase(Session.getCurrentUser().getRole())) {
            // Redirect non-admins home
            SceneNavigator.navigate(Views.HOME);
            return;
        }
        setupCostumeChoices();
        loadCurrentValues();
    }

    private void setupCostumeChoices() {
        var costumes = javafx.collections.FXCollections.observableArrayList(costumeDAO.getAll());
        cbo1.setItems(costumes); cbo2.setItems(costumes); cbo3.setItems(costumes); cbo4.setItems(costumes);
        // Display name in combo box
        java.util.function.Function<Costume, String> toText = c -> c == null ? "" : (c.getId() + ": " + c.getName());
        setComboRender(cbo1, toText); setComboRender(cbo2, toText); setComboRender(cbo3, toText); setComboRender(cbo4, toText);
    }

    private void setComboRender(ComboBox<Costume> combo, java.util.function.Function<Costume, String> toText) {
        combo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Costume item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : toText.apply(item));
            }
        });
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Costume item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : toText.apply(item));
            }
        });
    }

    private void loadCurrentValues() {
        setFromItem(1, cbo1, title1);
        setFromItem(2, cbo2, title2);
        setFromItem(3, cbo3, title3);
        setFromItem(4, cbo4, title4);
    }

    private void setFromItem(int slot, ComboBox<Costume> combo, TextField titleField) {
        var item = dao.get(slot);
        if (item != null) {
            if (item.getCostumeId() != null) {
                costumeDAO.findById(item.getCostumeId()).ifPresent(combo::setValue);
            }
            titleField.setText(item.getTitle());
        }
    }

    @FXML
    private void save() {
        boolean ok = true;
    ok &= saveSlot(1, cbo1, title1);
    ok &= saveSlot(2, cbo2, title2);
    ok &= saveSlot(3, cbo3, title3);
    ok &= saveSlot(4, cbo4, title4);

        Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Save Featured Images");
        alert.setHeaderText(null);
        alert.setContentText(ok ? "Featured images saved." : "Failed to save one or more entries.");
        alert.show();
    }

    private boolean saveSlot(int slot, ComboBox<Costume> combo, TextField titleField) {
        Costume c = combo.getValue();
        Integer cid = (c == null) ? null : c.getId();
        FeaturedItem fi = new FeaturedItem(slot, null, sanitize(titleField.getText()));
        fi.setCostumeId(cid);
        return dao.save(fi);
    }

    private String sanitize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    @FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
}
