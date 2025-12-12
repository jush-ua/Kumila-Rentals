package com.cosplay.util;

/**
 * Utility to export database data to seed_data.sql
 * Run this whenever you want to share your current database state via Git
 */
public class DataExporter {
    public static void main(String[] args) {
        System.out.println("Starting database export...");
        Database.exportAllData();
        System.out.println("\nDone! Check src/main/resources/db/seed_data.sql");
        System.out.println("You can now:");
        System.out.println("  1. Commit the file to Git");
        System.out.println("  2. Push to your repository");
        System.out.println("  3. Other developers can pull and run Database.loadSeedData()");
    }
}
