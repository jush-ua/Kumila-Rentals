package com.cosplay.model;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private int conversationId;
    private int senderId;
    private String senderName;
    private String senderEmail;
    private String message;
    private LocalDateTime timestamp;
    private boolean isAdminReply;
    private String status; // "unread", "read"
    
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }
    
    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }
    
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isAdminReply() { return isAdminReply; }
    public void setAdminReply(boolean adminReply) { isAdminReply = adminReply; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", senderName='" + senderName + '\'' +
                ", isAdminReply=" + isAdminReply +
                ", status='" + status + '\'' +
                '}';
    }
}
