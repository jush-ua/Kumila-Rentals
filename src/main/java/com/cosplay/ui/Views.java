package com.cosplay.ui;

/** Defines application views and their FXML resource paths. */
public enum Views {
    // Make LOGIN and REGISTER not resizable; other views remain resizable by default
    LOGIN("/com/cosplay/ui/views/LoginView.fxml", "Kumila Rentals - Login", false),
    REGISTER("/com/cosplay/ui/views/RegisterView.fxml", "Kumila Rentals - Register", false),
    HOME("/com/cosplay/ui/views/HomeView.fxml", "Kumila Rentals - Home", true),
    CATALOG("/com/cosplay/ui/views/CatalogView.fxml", "Kumila Rentals - Catalog", true),
    TERMS("/com/cosplay/ui/views/TermsView.fxml", "Kumila Rentals - Terms", true),
    ABOUT("/com/cosplay/ui/views/AboutView.fxml", "Kumila Rentals - About", true),
    ADMIN("/com/cosplay/ui/views/AdminView.fxml", "Kumila Rentals - Admin", true);

    private final String resource;
    private final String title;
    private final boolean resizable;

    Views(String resource, String title, boolean resizable) {
        this.resource = resource;
        this.title = title;
        this.resizable = resizable;
    }

    public String getResource() { return resource; }
    public String getTitle() { return title; }
    public boolean isResizable() { return resizable; }
}
