package com.cosplay.util;

import com.cosplay.dao.EventBannerDAO;
import com.cosplay.model.EventBanner;

/**
 * Utility to add an event banner to the database
 */
public class AddEventBanner {
    public static void main(String[] args) {
        // Initialize database
        Database.init();
        
        // Create a new event banner
        EventBanner banner = new EventBanner();
        banner.setTitle("Welcome to Kumila Rentals!");
        banner.setMessage("Browse our collection of premium cosplay rentals");
        banner.setActive(true);
        
        // Save to database
        EventBannerDAO dao = new EventBannerDAO();
        boolean success = dao.save(banner);
        
        if (success) {
            System.out.println("Event banner added successfully!");
        } else {
            System.out.println("Failed to add event banner.");
        }
    }
}
