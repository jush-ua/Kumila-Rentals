package com.cosplay.dao;

import com.cosplay.model.FeaturedItem;
import com.cosplay.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeaturedDAO {

    public List<FeaturedItem> listAll() {
        String sql = "SELECT slot, image_url, title, cosplay_id FROM featured_images ORDER BY slot";
        List<FeaturedItem> items = new ArrayList<>();
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeaturedItem fi = new FeaturedItem(
                            rs.getInt("slot"),
                            rs.getString("image_url"),
                            rs.getString("title")
                    );
                    int cid = rs.getInt("cosplay_id");
                    if (!rs.wasNull()) fi.setCosplayId(cid);
                    items.add(fi);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public FeaturedItem get(int slot) {
        String sql = "SELECT slot, image_url, title, cosplay_id FROM featured_images WHERE slot = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FeaturedItem fi = new FeaturedItem(
                            rs.getInt("slot"),
                            rs.getString("image_url"),
                            rs.getString("title")
                    );
                    int cid = rs.getInt("cosplay_id");
                    if (!rs.wasNull()) fi.setCosplayId(cid);
                    return fi;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean save(FeaturedItem item) {
        String sql = "INSERT INTO featured_images(slot, image_url, title, cosplay_id) VALUES(?, ?, ?, ?) " +
                     "ON CONFLICT(slot) DO UPDATE SET image_url=excluded.image_url, title=excluded.title, cosplay_id=excluded.cosplay_id";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getSlot());
            ps.setString(2, item.getImageUrl());
            ps.setString(3, item.getTitle());
            if (item.getCosplayId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, item.getCosplayId());
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}


