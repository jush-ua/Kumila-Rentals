package com.cosplay.dao;

import com.cosplay.model.Conversation;
import com.cosplay.model.Message;
import com.cosplay.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDAO {
    
    /**
     * Get or create a conversation for a user
     */
    public int getOrCreateConversation(int userId, String userName, String userEmail) {
        // First check if conversation exists
        String checkSql = "SELECT conversation_id FROM conversations WHERE user_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("conversation_id");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking conversation: " + e.getMessage());
        }
        
        // Create new conversation
        String insertSql = "INSERT INTO conversations (user_id, user_name, user_email, created_at, unread_count) " +
                          "VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, userName);
            pstmt.setString(3, userEmail);
            pstmt.setString(4, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            
            if (keys.next()) {
                return keys.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Send a message in a conversation
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (conversation_id, sender_id, sender_name, sender_email, " +
                     "message, timestamp, is_admin_reply, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, message.getConversationId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getSenderName());
            pstmt.setString(4, message.getSenderEmail());
            pstmt.setString(5, message.getMessage());
            pstmt.setString(6, message.getTimestamp().toString());
            pstmt.setInt(7, message.isAdminReply() ? 1 : 0);
            pstmt.setString(8, message.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update conversation last message
                updateConversationLastMessage(message.getConversationId(), message.getMessage(), 
                                             message.getTimestamp(), message.isAdminReply());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update conversation with last message info
     */
    private void updateConversationLastMessage(int conversationId, String lastMessage, 
                                               LocalDateTime timestamp, boolean isAdminReply) {
        String sql = "UPDATE conversations SET last_message = ?, last_message_time = ?, " +
                     "unread_count = CASE WHEN ? = 0 THEN unread_count + 1 ELSE unread_count END " +
                     "WHERE conversation_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, lastMessage.length() > 100 ? lastMessage.substring(0, 100) + "..." : lastMessage);
            pstmt.setString(2, timestamp.toString());
            pstmt.setInt(3, isAdminReply ? 1 : 0);
            pstmt.setInt(4, conversationId);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating conversation: " + e.getMessage());
        }
    }
    
    /**
     * Get all conversations (for admin)
     */
    public List<Conversation> getAllConversations() {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT * FROM conversations ORDER BY last_message_time DESC, created_at DESC";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching conversations: " + e.getMessage());
        }
        
        return conversations;
    }
    
    /**
     * Get conversation by user ID
     */
    public Optional<Conversation> getConversationByUserId(int userId) {
        String sql = "SELECT * FROM conversations WHERE user_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToConversation(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching conversation: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get messages in a conversation
     */
    public List<Message> getMessagesByConversation(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY timestamp ASC";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * Mark conversation as read (reset unread count)
     */
    public boolean markConversationAsRead(int conversationId) {
        String sql = "UPDATE conversations SET unread_count = 0 WHERE conversation_id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, conversationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking conversation as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get total unread count across all conversations
     */
    public int getTotalUnreadCount() {
        String sql = "SELECT SUM(unread_count) FROM conversations";
        
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
     * Delete a conversation and all its messages
     */
    public boolean deleteConversation(int conversationId) {
        try (Connection conn = Database.connect()) {
            // Delete messages first
            String deleteMessages = "DELETE FROM messages WHERE conversation_id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(deleteMessages);
            pstmt1.setInt(1, conversationId);
            pstmt1.executeUpdate();
            
            // Delete conversation
            String deleteConv = "DELETE FROM conversations WHERE conversation_id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(deleteConv);
            pstmt2.setInt(1, conversationId);
            int affectedRows = pstmt2.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting conversation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setSenderName(rs.getString("sender_name"));
        message.setSenderEmail(rs.getString("sender_email"));
        message.setMessage(rs.getString("message"));
        message.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        message.setAdminReply(rs.getInt("is_admin_reply") == 1);
        message.setStatus(rs.getString("status"));
        return message;
    }
    
    /**
     * Map ResultSet to Conversation object
     */
    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conversation = new Conversation();
        conversation.setConversationId(rs.getInt("conversation_id"));
        conversation.setUserId(rs.getInt("user_id"));
        conversation.setUserName(rs.getString("user_name"));
        conversation.setUserEmail(rs.getString("user_email"));
        conversation.setLastMessage(rs.getString("last_message"));
        
        String lastMsgTime = rs.getString("last_message_time");
        if (lastMsgTime != null && !lastMsgTime.isEmpty()) {
            conversation.setLastMessageTime(LocalDateTime.parse(lastMsgTime));
        }
        
        conversation.setUnreadCount(rs.getInt("unread_count"));
        conversation.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return conversation;
    }
}
