package com.cosplay.util;

/**
 * Performance configuration for the application.
 * Adjust these settings based on the target device capabilities.
 */
public class PerformanceConfig {
    // Image cache settings
    public static final int MAX_IMAGE_CACHE_SIZE = 100;
    
    // Image quality settings (higher = better quality, more memory)
    public static final int CATALOG_IMAGE_WIDTH = 300;  // Higher than display size for quality
    public static final int CATALOG_IMAGE_HEIGHT = 390;
    public static final int FEATURED_IMAGE_WIDTH = 280;
    public static final int FEATURED_IMAGE_HEIGHT = 400;
    public static final boolean HIGH_QUALITY_SCALING = true; // Use smooth scaling
    
    // Catalog pagination
    public static final int CATALOG_ITEMS_PER_PAGE = 20;
    
    // Database connection pool
    public static final int DB_POOL_MAX_SIZE = 10;
    public static final int DB_POOL_MIN_IDLE = 2;
    
    // Image loading settings
    public static final boolean ASYNC_IMAGE_LOADING = true;
    public static final boolean SCALE_IMAGES_ON_LOAD = true;
    
    // UI refresh rates (milliseconds)
    public static final int UI_UPDATE_DELAY = 100;
    
    /**
     * Get memory usage in MB
     */
    public static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
    
    /**
     * Get total available memory in MB
     */
    public static long getTotalMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() / (1024 * 1024);
    }
    
    /**
     * Get max memory in MB
     */
    public static long getMaxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() / (1024 * 1024);
    }
    
    /**
     * Print memory statistics to console
     */
    public static void logMemoryStats() {
        System.out.println("=== Memory Statistics ===");
        System.out.println("Used: " + getMemoryUsage() + " MB");
        System.out.println("Total: " + getTotalMemory() + " MB");
        System.out.println("Max: " + getMaxMemory() + " MB");
        System.out.println("========================");
    }
    
    /**
     * Request garbage collection (use sparingly)
     */
    public static void requestGC() {
        System.gc();
    }
}
