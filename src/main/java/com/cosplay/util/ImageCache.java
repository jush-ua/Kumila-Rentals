package com.cosplay.util;

import javafx.scene.image.Image;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.SoftReference;

/**
 * Image cache that stores loaded images using soft references
 * to prevent memory issues while still providing performance benefits.
 * Soft references allow the GC to reclaim memory when needed.
 */
public class ImageCache {
    private static final ConcurrentHashMap<String, SoftReference<Image>> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = PerformanceConfig.MAX_IMAGE_CACHE_SIZE;
    
    /**
     * Get an image from cache or load it if not cached
     * @param imagePath the path or URL to the image
     * @param backgroundLoading whether to load the image in background
     * @return the cached or newly loaded image, or null if loading fails
     */
    public static Image getImage(String imagePath, boolean backgroundLoading) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        
        // Check cache first
        SoftReference<Image> ref = cache.get(imagePath);
        if (ref != null) {
            Image cachedImage = ref.get();
            if (cachedImage != null && !cachedImage.isError()) {
                return cachedImage;
            } else {
                // Reference was cleared or image errored, remove from cache
                cache.remove(imagePath);
            }
        }
        
        // Load image
        try {
            Image image = loadImage(imagePath, backgroundLoading);
            if (image != null && !image.isError()) {
                // Only cache if we haven't exceeded max size
                if (cache.size() < MAX_CACHE_SIZE) {
                    cache.put(imagePath, new SoftReference<>(image));
                } else {
                    // Cache is full, remove oldest entries
                    cleanupCache();
                    cache.put(imagePath, new SoftReference<>(image));
                }
                return image;
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imagePath + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Load an image with smaller dimensions for thumbnails/cards
     * @param imagePath the path or URL to the image
     * @param requestedWidth the requested width (will preserve aspect ratio)
     * @param requestedHeight the requested height (will preserve aspect ratio)
     * @param backgroundLoading whether to load in background
     * @return the loaded image
     */
    public static Image getImageScaled(String imagePath, double requestedWidth, double requestedHeight, boolean backgroundLoading) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        
        // Create a cache key with dimensions
        String cacheKey = imagePath + "_" + requestedWidth + "x" + requestedHeight;
        
        // Check cache
        SoftReference<Image> ref = cache.get(cacheKey);
        if (ref != null) {
            Image cachedImage = ref.get();
            if (cachedImage != null && !cachedImage.isError()) {
                return cachedImage;
            } else {
                cache.remove(cacheKey);
            }
        }
        
        // Load scaled image
        try {
            Image image = loadImageScaled(imagePath, requestedWidth, requestedHeight, backgroundLoading);
            if (image != null && !image.isError()) {
                if (cache.size() < MAX_CACHE_SIZE) {
                    cache.put(cacheKey, new SoftReference<>(image));
                } else {
                    cleanupCache();
                    cache.put(cacheKey, new SoftReference<>(image));
                }
                return image;
            }
        } catch (Exception e) {
            System.err.println("Failed to load scaled image: " + imagePath + " - " + e.getMessage());
        }
        
        return null;
    }
    
    private static Image loadImage(String imagePath, boolean backgroundLoading) {
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return new Image(imagePath, backgroundLoading);
        } else {
            java.io.File imageFile = new java.io.File(imagePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), backgroundLoading);
            } else {
                System.err.println("Image file not found: " + imagePath);
                return null;
            }
        }
    }
    
    private static Image loadImageScaled(String imagePath, double requestedWidth, double requestedHeight, boolean backgroundLoading) {
        // Use smooth=true (5th parameter) for high-quality scaling
        // preserveRatio=true (4th parameter) maintains aspect ratio
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return new Image(imagePath, requestedWidth, requestedHeight, true, true, backgroundLoading);
        } else {
            java.io.File imageFile = new java.io.File(imagePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), requestedWidth, requestedHeight, true, true, backgroundLoading);
            } else {
                System.err.println("Image file not found: " + imagePath);
                return null;
            }
        }
    }
    
    /**
     * Remove entries with cleared references
     */
    private static void cleanupCache() {
        cache.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }
    
    /**
     * Clear the entire cache
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * Get current cache size
     */
    public static int getCacheSize() {
        cleanupCache();
        return cache.size();
    }
}
