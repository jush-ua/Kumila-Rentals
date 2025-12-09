package com.cosplay.dao;

import com.cosplay.model.EventBanner;
import com.cosplay.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventBannerDAO {
    
    /**
     * Get the currently active event banner (if any)
     */
    public Optional<EventBanner> getActiveBanner() {
        String sql = "SELECT * FROM event_banners WHERE is_active = 1 LIMIT 1";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBanner(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active banner: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get a specific banner by ID
     */
    public Optional<EventBanner> getById(int id) {
        String sql = "SELECT * FROM event_banners WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBanner(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting banner by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all event banners (for admin management)
     */
    public List<EventBanner> getAll() {
        List<EventBanner> banners = new ArrayList<>();
        String sql = "SELECT * FROM event_banners ORDER BY id DESC";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                banners.add(mapResultSetToBanner(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all banners: " + e.getMessage());
        }
        
        return banners;
    }
    
    /**
     * Save a new banner or update an existing one
     */
    public boolean save(EventBanner banner) {
        if (banner.getId() == null) {
            return insert(banner);
        } else {
            return update(banner);
        }
    }
    
    /**
     * Insert a new banner
     */
    private boolean insert(EventBanner banner) {
        String sql = "INSERT INTO event_banners (title, message, is_active, background_color, text_color, link_url, link_text) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // If this banner is being set as active, deactivate all others first
            if (banner.isActive()) {
                deactivateAll();
            }
            
            pstmt.setString(1, banner.getTitle());
            pstmt.setString(2, banner.getMessage());
            pstmt.setBoolean(3, banner.isActive());
            pstmt.setString(4, banner.getBackgroundColor());
            pstmt.setString(5, banner.getTextColor());
            pstmt.setString(6, banner.getLinkUrl());
            pstmt.setString(7, banner.getLinkText());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting banner: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update an existing banner
     */
    private boolean update(EventBanner banner) {
        String sql = "UPDATE event_banners SET title = ?, message = ?, is_active = ?, " +
                     "background_color = ?, text_color = ?, link_url = ?, link_text = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // If this banner is being set as active, deactivate all others first
            if (banner.isActive()) {
                deactivateAll();
            }
            
            pstmt.setString(1, banner.getTitle());
            pstmt.setString(2, banner.getMessage());
            pstmt.setBoolean(3, banner.isActive());
            pstmt.setString(4, banner.getBackgroundColor());
            pstmt.setString(5, banner.getTextColor());
            pstmt.setString(6, banner.getLinkUrl());
            pstmt.setString(7, banner.getLinkText());
            pstmt.setInt(8, banner.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating banner: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a banner
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM event_banners WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting banner: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deactivate all banners (used when activating a new one)
     */
    private void deactivateAll() {
        String sql = "UPDATE event_banners SET is_active = 0";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error deactivating banners: " + e.getMessage());
        }
    }
    
    /**
     * Toggle active status of a banner
     */
    public boolean toggleActive(int id) {
        Optional<EventBanner> bannerOpt = getById(id);
        if (bannerOpt.isPresent()) {
            EventBanner banner = bannerOpt.get();
            
            if (!banner.isActive()) {
                // Activating this banner - deactivate all others first
                deactivateAll();
            }
            
            String sql = "UPDATE event_banners SET is_active = ? WHERE id = ?";
            
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setBoolean(1, !banner.isActive());
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Error toggling banner active status: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Map a ResultSet row to an EventBanner object
     */
    private EventBanner mapResultSetToBanner(ResultSet rs) throws SQLException {
        EventBanner banner = new EventBanner();
        banner.setId(rs.getInt("id"));
        banner.setTitle(rs.getString("title"));
        banner.setMessage(rs.getString("message"));
        banner.setActive(rs.getBoolean("is_active"));
        banner.setBackgroundColor(rs.getString("background_color"));
        banner.setTextColor(rs.getString("text_color"));
        banner.setLinkUrl(rs.getString("link_url"));
        banner.setLinkText(rs.getString("link_text"));
        return banner;
    }
}
