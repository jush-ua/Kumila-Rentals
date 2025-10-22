package com.cosplay.dao;

import com.cosplay.model.User;
import com.cosplay.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

	public User login(String username, String password) {
		String sql = "SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, password);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					User u = new User();
					u.setUserId(rs.getInt("user_id"));
					u.setUsername(rs.getString("username"));
					u.setPassword(rs.getString("password"));
					u.setEmail(rs.getString("email"));
					u.setRole(rs.getString("role"));
					return u;
				}
			}
		} catch (SQLException e) { e.printStackTrace(); }
		return null;
	}

	public boolean createUser(User u) {
		String sql = "INSERT INTO users(username, password, email, role) VALUES (?, ?, ?, ?)";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, u.getUsername());
			ps.setString(2, u.getPassword());
			ps.setString(3, u.getEmail());
			ps.setString(4, u.getRole() == null ? "customer" : u.getRole());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) { e.printStackTrace(); return false; }
	}
}
