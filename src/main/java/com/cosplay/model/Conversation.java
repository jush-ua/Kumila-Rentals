package com.cosplay.model;

import java.time.LocalDateTime;

public class Conversation {
    private int conversationId;
    private int userId;
    private String userName;
    private String userEmail;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private LocalDateTime createdAt;
    
    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId=" + conversationId +
                ", userName='" + userName + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
