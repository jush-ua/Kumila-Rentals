package com.cosplay.dao;

import com.cosplay.model.Message;
import com.cosplay.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDAO {
    
    /**
     * Send a message to admin
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, sender_name, sender_email, subject, message, timestamp, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, message.getSenderId());
            pstmt.setString(2, message.getSenderName());
            pstmt.setString(3, message.getSenderEmail());
            pstmt.setString(4, message.getSubject());
            pstmt.setString(5, message.getMessage());
            pstmt.setString(6, message.getTimestamp().toString());
            pstmt.setString(7, message.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all messages (admin view)
     */
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY timestamp DESC";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * Get messages by user ID
     */
    public List<Message> getMessagesByUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE sender_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching user messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * Get unread message count
     */
    public int getUnreadCount() {
        String sql = "SELECT COUNT(*) FROM messages WHERE status = 'unread'";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Mark message as read
     */
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE messages SET status = 'read' WHERE message_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark message as replied
     */
    public boolean markAsReplied(int messageId) {
        String sql = "UPDATE messages SET status = 'replied' WHERE message_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking message as replied: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get message by ID
     */
    public Optional<Message> getMessageById(int messageId) {
        String sql = "SELECT * FROM messages WHERE message_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToMessage(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching message: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete a message
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE message_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setSenderName(rs.getString("sender_name"));
        message.setSenderEmail(rs.getString("sender_email"));
        message.setSubject(rs.getString("subject"));
        message.setMessage(rs.getString("message"));
        message.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        message.setStatus(rs.getString("status"));
        return message;
    }
}
