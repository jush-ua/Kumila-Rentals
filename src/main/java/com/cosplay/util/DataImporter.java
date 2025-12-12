package com.cosplay.util;

/**
 * Utility to import database data from seed_data.sql
 * Run this after cloning/pulling the repository to set up the database
 */
public class DataImporter {
    public static void main(String[] args) {
        System.out.println("Starting database import...");
        System.out.println("This will load data from: src/main/resources/db/seed_data.sql");
        System.out.println();
        
        Database.init(); // Create tables first
        Database.loadSeedData(); // Then load the data
        
        System.out.println("\nDone! Your database is ready to use.");
        System.out.println("You can now run the application.");
    }
}
