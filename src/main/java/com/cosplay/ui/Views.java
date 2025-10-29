package com.cosplay.ui;

/** Defines application views and their FXML resource paths. */
public enum Views {
    LOGIN("/com/cosplay/ui/views/LoginView.fxml", "Kumila Rentals - Login"),
    REGISTER("/com/cosplay/ui/views/RegisterView.fxml", "Kumila Rentals - Register"),
    HOME("/com/cosplay/ui/views/HomeView.fxml", "Kumila Rentals - Home"),
    CATALOG("/com/cosplay/ui/views/CatalogView.fxml", "Kumila Rentals - Catalog"),
    TERMS("/com/cosplay/ui/views/TermsView.fxml", "Kumila Rentals - Terms"),
    ABOUT("/com/cosplay/ui/views/AboutView.fxml", "Kumila Rentals - About"),
    ADMIN("/com/cosplay/ui/views/AdminView.fxml", "Kumila Rentals - Admin");

    private final String resource;
    private final String title;

    Views(String resource, String title) {
        this.resource = resource;
        this.title = title;
    }

    public String getResource() { return resource; }
    public String getTitle() { return title; }
}
