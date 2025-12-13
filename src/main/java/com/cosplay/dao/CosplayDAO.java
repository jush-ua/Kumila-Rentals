package com.cosplay.dao;

import com.cosplay.model.Cosplay;
import com.cosplay.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CosplayDAO {

    public void addCosplay(Cosplay c) {
        // Only admins are allowed to add cosplays
        var user = com.cosplay.util.Session.getCurrentUser();
        if (user == null || user.getRole() == null || !"admin".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Only admin users can add cosplays.");
        }
        String sql = "INSERT INTO cosplays(name, category, series_name, size, description, image_path, rent_rate_1day, rent_rate_2days, rent_rate_3days, add_ons) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getCategory());
            ps.setString(3, c.getSeriesName());
            ps.setString(4, c.getSize());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getImagePath());
            if (c.getRentRate1Day() != null) {
                ps.setDouble(7, c.getRentRate1Day());
            } else {
                ps.setNull(7, java.sql.Types.REAL);
            }
            if (c.getRentRate2Days() != null) {
                ps.setDouble(8, c.getRentRate2Days());
            } else {
                ps.setNull(8, java.sql.Types.REAL);
            }
            if (c.getRentRate3Days() != null) {
                ps.setDouble(9, c.getRentRate3Days());
            } else {
                ps.setNull(9, java.sql.Types.REAL);
            }
            ps.setString(10, c.getAddOns());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Cosplay> getAll() {
        List<Cosplay> list = new ArrayList<>();
        String sql = "SELECT * FROM cosplays";
        try (Connection conn = Database.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Cosplay c = new Cosplay();
                c.setId(rs.getInt("cosplay_id"));
                c.setName(rs.getString("name"));
                c.setCategory(rs.getString("category"));
                c.setSeriesName(rs.getString("series_name"));
                c.setSize(rs.getString("size"));
                c.setDescription(rs.getString("description"));
                c.setImagePath(rs.getString("image_path"));
                c.setRentRate1Day(rs.getObject("rent_rate_1day") != null ? rs.getDouble("rent_rate_1day") : null);
                c.setRentRate2Days(rs.getObject("rent_rate_2days") != null ? rs.getDouble("rent_rate_2days") : null);
                c.setRentRate3Days(rs.getObject("rent_rate_3days") != null ? rs.getDouble("rent_rate_3days") : null);
                c.setAddOns(rs.getString("add_ons"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Cosplay> findById(int id) {
        String sql = "SELECT * FROM cosplays WHERE cosplay_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cosplay c = new Cosplay();
                    c.setId(rs.getInt("cosplay_id"));
                    c.setName(rs.getString("name"));
                    c.setCategory(rs.getString("category"));
                    c.setSeriesName(rs.getString("series_name"));
                    c.setSize(rs.getString("size"));
                    c.setDescription(rs.getString("description"));
                    c.setImagePath(rs.getString("image_path"));
                    c.setRentRate1Day(rs.getObject("rent_rate_1day") != null ? rs.getDouble("rent_rate_1day") : null);
                    c.setRentRate2Days(rs.getObject("rent_rate_2days") != null ? rs.getDouble("rent_rate_2days") : null);
                    c.setRentRate3Days(rs.getObject("rent_rate_3days") != null ? rs.getDouble("rent_rate_3days") : null);
                    c.setAddOns(rs.getString("add_ons"));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    public List<Cosplay> searchByCategory(String category) {
        List<Cosplay> list = new ArrayList<>();
        String sql = "SELECT * FROM cosplays WHERE category LIKE ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + category + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cosplay c = new Cosplay();
                    c.setId(rs.getInt("cosplay_id"));
                    c.setName(rs.getString("name"));
                    c.setCategory(rs.getString("category"));
                    c.setSeriesName(rs.getString("series_name"));
                    c.setSize(rs.getString("size"));
                    c.setDescription(rs.getString("description"));
                    c.setImagePath(rs.getString("image_path"));
                    c.setRentRate1Day(rs.getObject("rent_rate_1day") != null ? rs.getDouble("rent_rate_1day") : null);
                    c.setRentRate2Days(rs.getObject("rent_rate_2days") != null ? rs.getDouble("rent_rate_2days") : null);
                    c.setRentRate3Days(rs.getObject("rent_rate_3days") != null ? rs.getDouble("rent_rate_3days") : null);
                    c.setAddOns(rs.getString("add_ons"));
                    list.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateCosplay(Cosplay c) {
        var user = com.cosplay.util.Session.getCurrentUser();
        if (user == null || user.getRole() == null || !"admin".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Only admin users can update cosplays.");
        }
        String sql = "UPDATE cosplays SET name = ?, category = ?, series_name = ?, size = ?, description = ?, image_path = ?, rent_rate_1day = ?, rent_rate_2days = ?, rent_rate_3days = ?, add_ons = ? WHERE cosplay_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getCategory());
            ps.setString(3, c.getSeriesName());
            ps.setString(4, c.getSize());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getImagePath());
            if (c.getRentRate1Day() != null) {
                ps.setDouble(7, c.getRentRate1Day());
            } else {
                ps.setNull(7, java.sql.Types.REAL);
            }
            if (c.getRentRate2Days() != null) {
                ps.setDouble(8, c.getRentRate2Days());
            } else {
                ps.setNull(8, java.sql.Types.REAL);
            }
            if (c.getRentRate3Days() != null) {
                ps.setDouble(9, c.getRentRate3Days());
            } else {
                ps.setNull(9, java.sql.Types.REAL);
            }
            ps.setString(10, c.getAddOns());
            ps.setInt(11, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCosplay(int id) {
        var user = com.cosplay.util.Session.getCurrentUser();
        if (user == null || user.getRole() == null || !"admin".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Only admin users can delete cosplays.");
        }
        String sql = "DELETE FROM cosplays WHERE cosplay_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getDistinctCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM cosplays WHERE category IS NOT NULL ORDER BY category";
        try (Connection conn = Database.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}

