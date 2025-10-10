package com.cosplay.dao;

import com.cosplay.model.Costume;
import com.cosplay.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CostumeDAO {

    public void addCostume(Costume c) {
        String sql = "INSERT INTO costumes(name, category, size, description, image_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getCategory());
            ps.setString(3, c.getSize());
            ps.setString(4, c.getDescription());
            ps.setString(5, c.getImagePath());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Costume> getAll() {
        List<Costume> list = new ArrayList<>();
        String sql = "SELECT * FROM costumes";
        try (Connection conn = Database.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Costume c = new Costume();
                c.setId(rs.getInt("costume_id"));
                c.setName(rs.getString("name"));
                c.setCategory(rs.getString("category"));
                c.setSize(rs.getString("size"));
                c.setDescription(rs.getString("description"));
                c.setImagePath(rs.getString("image_path"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Costume> findById(int id) {
        String sql = "SELECT * FROM costumes WHERE costume_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Costume c = new Costume();
                    c.setId(rs.getInt("costume_id"));
                    c.setName(rs.getString("name"));
                    c.setCategory(rs.getString("category"));
                    c.setSize(rs.getString("size"));
                    c.setDescription(rs.getString("description"));
                    c.setImagePath(rs.getString("image_path"));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    public List<Costume> searchByCategory(String category) {
        List<Costume> list = new ArrayList<>();
        String sql = "SELECT * FROM costumes WHERE category LIKE ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + category + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Costume c = new Costume();
                    c.setId(rs.getInt("costume_id"));
                    c.setName(rs.getString("name"));
                    c.setCategory(rs.getString("category"));
                    c.setSize(rs.getString("size"));
                    c.setDescription(rs.getString("description"));
                    c.setImagePath(rs.getString("image_path"));
                    list.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
