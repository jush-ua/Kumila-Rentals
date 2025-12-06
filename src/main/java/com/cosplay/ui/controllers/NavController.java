package com.cosplay.ui.controllers;

import com.cosplay.ui.SceneNavigator;
import com.cosplay.ui.Views;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavController {
	@FXML private Button btnHome;
	@FXML private Button btnCatalog;
	@FXML private Button btnAbout;
	@FXML private Button btnAdmin;
	@FXML private Button btnLogout;

	@FXML
	private void initialize() {
		// Ensure base class is applied once when loaded (FXML already sets it)
		// Active highlight will be set by parent controller via setActive(...)
		clearActive();
		updateSessionVisibility();
	}

	private void clearActive() {
		btnHome.getStyleClass().remove("active");
		btnCatalog.getStyleClass().remove("active");
		btnAbout.getStyleClass().remove("active");
	}

	public void setActive(Views view) {
		clearActive();
		switch (view) {
			case HOME -> btnHome.getStyleClass().add("active");
			case CATALOG -> btnCatalog.getStyleClass().add("active");
			case ABOUT -> btnAbout.getStyleClass().add("active");
			case ADMIN -> btnAdmin.getStyleClass().add("active");
			default -> {}
		}
	}

	// Navigation handlers
	@FXML private void goHome() { SceneNavigator.navigate(Views.HOME); }
	@FXML private void goCatalog() { SceneNavigator.navigate(Views.CATALOG); }
	@FXML private void goAbout() { SceneNavigator.navigate(Views.ABOUT); }
	@FXML private void goAdmin() { SceneNavigator.navigate(Views.ADMIN); }

	@FXML private void logout() {
		com.cosplay.util.Session.clear();
		SceneNavigator.navigate(Views.LOGIN);
	}

	private void updateSessionVisibility() {
		var user = com.cosplay.util.Session.getCurrentUser();
		boolean isLoggedIn = user != null;
		boolean isAdmin = isLoggedIn && "admin".equalsIgnoreCase(user.getRole());
		if (btnAdmin != null) {
			btnAdmin.setVisible(isAdmin);
			btnAdmin.setManaged(isAdmin);
		}
		if (btnLogout != null) {
			btnLogout.setVisible(isLoggedIn);
			btnLogout.setManaged(isLoggedIn);
		}
	}
}

