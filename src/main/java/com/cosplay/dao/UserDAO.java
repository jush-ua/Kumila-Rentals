package com.cosplay.dao;

import com.cosplay.model.User;
import com.cosplay.util.Database;
import com.cosplay.util.PasswordUtil;
import com.cosplay.util.TokenUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

	/**
	 * Authenticate a user with username and password.
	 * @param username the username
	 * @param password the plain text password
	 * @return the User object if authentication successful, null otherwise
	 */
	public User login(String username, String password) {
		String sql = "SELECT * FROM users WHERE username = ? LIMIT 1";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String hashedPassword = rs.getString("password");
					// Verify password using BCrypt
					if (hashedPassword != null && PasswordUtil.verifyPassword(password, hashedPassword)) {
						return mapUser(rs);
					}
				}
			}
		} catch (SQLException e) { 
			e.printStackTrace(); 
		}
		return null;
	}

	/**
	 * Create a new user in the database with email verification token.
	 * @param u the User object with plain text password
	 * @return true if user created successfully, false otherwise
	 */
	public boolean createUser(User u) {
		String sql = "INSERT INTO users(username, password, email, role, email_verified, verification_token, oauth_provider) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, u.getUsername());
			// Hash password before storing (can be null for OAuth users)
			ps.setString(2, u.getPassword() != null ? PasswordUtil.hashPassword(u.getPassword()) : null);
			ps.setString(3, u.getEmail());
			ps.setString(4, u.getRole() == null ? "customer" : u.getRole());
			ps.setInt(5, u.isEmailVerified() ? 1 : 0);
			ps.setString(6, u.getVerificationToken());
			ps.setString(7, u.getOauthProvider());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) { 
			e.printStackTrace(); 
			return false; 
		}
	}

	/**
	 * Check if a username already exists in the database.
	 * @param username the username to check
	 * @return true if username exists, false otherwise
	 */
	public boolean usernameExists(String username) {
		String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) { 
			e.printStackTrace(); 
		}
		return false;
	}

	/**
	 * Check if an email already exists in the database.
	 * @param email the email to check
	 * @return true if email exists, false otherwise
	 */
	public boolean emailExists(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) { 
			e.printStackTrace(); 
		}
		return false;
	}

	/**
	 * Verify a user's email using verification token.
	 * @param token the verification token
	 * @return true if verification successful, false otherwise
	 */
	public boolean verifyEmail(String token) {
		String sql = "UPDATE users SET email_verified = 1, verification_token = NULL WHERE verification_token = ?";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, token);
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) { 
			e.printStackTrace(); 
			return false; 
		}
	}

	/**
	 * Get user by verification token.
	 * @param token the verification token
	 * @return User object if found, null otherwise
	 */
	public User getUserByVerificationToken(String token) {
		String sql = "SELECT * FROM users WHERE verification_token = ? LIMIT 1";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, token);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapUser(rs);
				}
			}
		} catch (SQLException e) { 
			e.printStackTrace(); 
		}
		return null;
	}

	/**
	 * Find or create user from OAuth provider.
	 * @param oauthProvider the OAuth provider (e.g., "google")
	 * @param oauthId the user's ID from the provider
	 * @param email the user's email
	 * @param username the username (generated from email if needed)
	 * @return User object
	 */
	public User findOrCreateOAuthUser(String oauthProvider, String oauthId, String email, String username) {
		// First, try to find existing user by OAuth ID
		String findSql = "SELECT * FROM users WHERE oauth_provider = ? AND oauth_id = ? LIMIT 1";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(findSql)) {
			ps.setString(1, oauthProvider);
			ps.setString(2, oauthId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapUser(rs);
				}
			}
		} catch (SQLException e) { 
			e.printStackTrace(); 
		}

		// User doesn't exist, create new one
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setEmail(email);
		newUser.setRole("customer");
		newUser.setEmailVerified(true); // OAuth emails are pre-verified
		newUser.setOauthProvider(oauthProvider);
		newUser.setOauthId(oauthId);
		newUser.setPassword(null); // No password for OAuth users

		String insertSql = "INSERT INTO users(username, password, email, role, email_verified, oauth_provider, oauth_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(insertSql)) {
			ps.setString(1, newUser.getUsername());
			ps.setString(2, null);
			ps.setString(3, newUser.getEmail());
			ps.setString(4, newUser.getRole());
			ps.setInt(5, 1);
			ps.setString(6, newUser.getOauthProvider());
			ps.setString(7, oauthId);
			ps.executeUpdate();

			// Get the generated user ID
			try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					newUser.setUserId(generatedKeys.getInt(1));
				}
			}
			return newUser;
		} catch (SQLException e) { 
			e.printStackTrace(); 
			return null;
		}
	}

	/**
	 * Resend verification email by generating a new token.
	 * @param email the user's email
	 * @return the new verification token, or null if failed
	 */
	public String resendVerificationToken(String email) {
		String newToken = TokenUtil.generateToken();
		String sql = "UPDATE users SET verification_token = ? WHERE email = ? AND email_verified = 0";
		try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newToken);
			ps.setString(2, email);
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0 ? newToken : null;
		} catch (SQLException e) { 
			e.printStackTrace(); 
			return null; 
		}
	}

	/**
	 * Helper method to map ResultSet to User object.
	 */
	private User mapUser(ResultSet rs) throws SQLException {
		User u = new User();
		u.setUserId(rs.getInt("user_id"));
		u.setUsername(rs.getString("username"));
		u.setPassword(rs.getString("password"));
		u.setEmail(rs.getString("email"));
		u.setRole(rs.getString("role"));
		u.setEmailVerified(rs.getInt("email_verified") == 1);
		u.setVerificationToken(rs.getString("verification_token"));
		u.setOauthProvider(rs.getString("oauth_provider"));
		u.setOauthId(rs.getString("oauth_id"));
		return u;
	}
}
