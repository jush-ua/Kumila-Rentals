package com.cosplay.ui.controllers;

import com.cosplay.dao.MessageDAO;
import com.cosplay.model.Conversation;
import com.cosplay.model.Message;
import com.cosplay.model.User;
import com.cosplay.ui.Views;
import com.cosplay.util.Session;
import com.cosplay.util.StyledAlert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesController {
    @FXML private NavController navBarController;
    
    // User view components
    @FXML private VBox userChatSection;
    @FXML private ScrollPane userChatScrollPane;
    @FXML private VBox userChatContainer;
    @FXML private TextField txtUserMessage;
    @FXML private Button btnUserSend;
    @FXML private Label lblChatStatus;
    
    // Admin view components
    @FXML private HBox adminChatSection;
    @FXML private VBox conversationListContainer;
    @FXML private ScrollPane adminChatScrollPane;
    @FXML private VBox adminChatContainer;
    @FXML private TextField txtAdminReply;
    @FXML private Button btnAdminSend;
    @FXML private Label lblUnreadCount;
    @FXML private Label lblSelectedConversation;
    
    private MessageDAO messageDAO;
    private User currentUser;
    private boolean isAdmin;
    private int currentConversationId = -1;
    private int selectedUserId = -1;

    @FXML
    private void initialize() {
        if (navBarController != null) {
            navBarController.setActive(Views.MESSAGES);
        }
        
        messageDAO = new MessageDAO();
        currentUser = Session.getCurrentUser();
        isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        
        setupView();
        loadContent();
    }
    
    private void setupView() {
        if (isAdmin) {
            // Show admin view
            userChatSection.setManaged(false);
            userChatSection.setVisible(false);
            adminChatSection.setManaged(true);
            adminChatSection.setVisible(true);
        } else {
            // Show user view
            userChatSection.setManaged(true);
            userChatSection.setVisible(true);
            adminChatSection.setManaged(false);
            adminChatSection.setVisible(false);
        }
    }
    
    private void loadContent() {
        if (isAdmin) {
            loadConversationList();
            updateUnreadCount();
        } else {
            loadUserChat();
        }
    }
    
    /**
     * USER VIEW: Load chat with admin
     */
    private void loadUserChat() {
        userChatContainer.getChildren().clear();
        
        // Get or create conversation
        currentConversationId = messageDAO.getOrCreateConversation(
            currentUser.getUserId(), 
            currentUser.getUsername(), 
            currentUser.getEmail()
        );
        
        if (currentConversationId == -1) {
            showUserStatus("Error loading chat", "error");
            return;
        }
        
        // Load messages
        List<Message> messages = messageDAO.getMessagesByConversation(currentConversationId);
        
        if (messages.isEmpty()) {
            Label welcomeMsg = new Label("Start a conversation with the admin!");
            welcomeMsg.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 20;");
            userChatContainer.getChildren().add(welcomeMsg);
        } else {
            for (Message msg : messages) {
                userChatContainer.getChildren().add(createChatBubble(msg, !msg.isAdminReply()));
            }
        }
        
        // Scroll to bottom
        Platform.runLater(() -> userChatScrollPane.setVvalue(1.0));
    }
    
    /**
     * USER ACTION: Send message
     */
    @FXML
    private void sendUserMessage() {
        String messageText = txtUserMessage.getText().trim();
        
        if (messageText.isEmpty()) {
            showUserStatus("Please enter a message", "error");
            return;
        }
        
        // Create message
        Message message = new Message();
        message.setConversationId(currentConversationId);
        message.setSenderId(currentUser.getUserId());
        message.setSenderName(currentUser.getUsername());
        message.setSenderEmail(currentUser.getEmail());
        message.setMessage(messageText);
        message.setTimestamp(LocalDateTime.now());
        message.setAdminReply(false);
        message.setStatus("unread");
        
        if (messageDAO.sendMessage(message)) {
            txtUserMessage.clear();
            loadUserChat();
        } else {
            showUserStatus("Failed to send message", "error");
        }
    }
    
    /**
     * ADMIN VIEW: Load conversation list
     */
    private void loadConversationList() {
        conversationListContainer.getChildren().clear();
        List<Conversation> conversations = messageDAO.getAllConversations();
        
        if (conversations.isEmpty()) {
            Label noConv = new Label("No conversations yet");
            noConv.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 15;");
            conversationListContainer.getChildren().add(noConv);
        } else {
            for (Conversation conv : conversations) {
                conversationListContainer.getChildren().add(createConversationCard(conv));
            }
        }
    }
    
    /**
     * ADMIN VIEW: Create conversation card
     */
    private VBox createConversationCard(Conversation conv) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #ddd; " +
                     "-fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        // Highlight if selected
        if (conv.getConversationId() == currentConversationId) {
            card.setStyle(card.getStyle() + " -fx-background-color: #e3f2fd;");
        }
        
        // User name with unread badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label userName = new Label(conv.getUserName());
        userName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox.setHgrow(userName, Priority.ALWAYS);
        
        if (conv.getUnreadCount() > 0) {
            Label unreadBadge = new Label(String.valueOf(conv.getUnreadCount()));
            unreadBadge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                               "-fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            header.getChildren().addAll(userName, unreadBadge);
        } else {
            header.getChildren().add(userName);
        }
        
        // Last message preview
        if (conv.getLastMessage() != null) {
            Label lastMsg = new Label(conv.getLastMessage());
            lastMsg.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            lastMsg.setMaxWidth(Double.MAX_VALUE);
            lastMsg.setWrapText(false);
            
            card.getChildren().addAll(header, lastMsg);
        } else {
            card.getChildren().add(header);
        }
        
        // Click to select conversation
        card.setOnMouseClicked(e -> selectConversation(conv));
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            if (conv.getConversationId() != currentConversationId) {
                card.setStyle(card.getStyle().replace("white", "#f5f5f5"));
            }
        });
        card.setOnMouseExited(e -> {
            if (conv.getConversationId() != currentConversationId) {
                card.setStyle(card.getStyle().replace("#f5f5f5", "white"));
            }
        });
        
        return card;
    }
    
    /**
     * ADMIN ACTION: Select a conversation
     */
    private void selectConversation(Conversation conv) {
        currentConversationId = conv.getConversationId();
        selectedUserId = conv.getUserId();
        
        lblSelectedConversation.setText("Chat with " + conv.getUserName());
        
        // Mark as read
        messageDAO.markConversationAsRead(currentConversationId);
        
        // Reload lists
        loadConversationList();
        loadAdminChat();
        updateUnreadCount();
    }
    
    /**
     * ADMIN VIEW: Load chat messages for selected conversation
     */
    private void loadAdminChat() {
        adminChatContainer.getChildren().clear();
        
        if (currentConversationId == -1) {
            Label selectMsg = new Label("Select a conversation to view messages");
            selectMsg.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 20;");
            adminChatContainer.getChildren().add(selectMsg);
            return;
        }
        
        List<Message> messages = messageDAO.getMessagesByConversation(currentConversationId);
        
        if (messages.isEmpty()) {
            Label noMsg = new Label("No messages in this conversation");
            noMsg.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 20;");
            adminChatContainer.getChildren().add(noMsg);
        } else {
            for (Message msg : messages) {
                adminChatContainer.getChildren().add(createChatBubble(msg, msg.isAdminReply()));
            }
        }
        
        // Scroll to bottom
        Platform.runLater(() -> adminChatScrollPane.setVvalue(1.0));
    }
    
    /**
     * ADMIN ACTION: Send reply
     */
    @FXML
    private void sendAdminReply() {
        if (currentConversationId == -1) {
            return;
        }
        
        String messageText = txtAdminReply.getText().trim();
        
        if (messageText.isEmpty()) {
            return;
        }
        
        // Create admin reply
        Message message = new Message();
        message.setConversationId(currentConversationId);
        message.setSenderId(currentUser.getUserId());
        message.setSenderName("Admin");
        message.setSenderEmail(currentUser.getEmail());
        message.setMessage(messageText);
        message.setTimestamp(LocalDateTime.now());
        message.setAdminReply(true);
        message.setStatus("read");
        
        if (messageDAO.sendMessage(message)) {
            txtAdminReply.clear();
            loadAdminChat();
            loadConversationList();
        }
    }
    
    /**
     * Create a chat bubble for a message
     */
    private HBox createChatBubble(Message msg, boolean alignRight) {
        HBox container = new HBox();
        container.setPadding(new Insets(5, 10, 5, 10));
        
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(10, 15, 10, 15));
        
        if (alignRight) {
            // User's own messages (right side, blue)
            bubble.setStyle("-fx-background-color: #d47f47; -fx-background-radius: 15 15 5 15; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            container.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // Other's messages (left side, gray)
            bubble.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 15 15 15 5; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            container.setAlignment(Pos.CENTER_LEFT);
        }
        
        // Message text
        Label messageLabel = new Label(msg.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: " + (alignRight ? "white" : "#333") + "; " +
                             "-fx-font-size: 13px;");
        
        // Timestamp
        Label timeLabel = new Label(formatTime(msg.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: " + (alignRight ? "rgba(255,255,255,0.8)" : "#999") + "; " +
                          "-fx-font-size: 11px;");
        
        bubble.getChildren().addAll(messageLabel, timeLabel);
        container.getChildren().add(bubble);
        
        return container;
    }
    
    /**
     * Update unread count for admin
     */
    private void updateUnreadCount() {
        if (isAdmin) {
            int count = messageDAO.getTotalUnreadCount();
            Platform.runLater(() -> 
                lblUnreadCount.setText(count + " unread message" + (count != 1 ? "s" : ""))
            );
        }
    }
    
    /**
     * Refresh button action
     */
    @FXML
    private void refreshMessages() {
        if (isAdmin) {
            loadConversationList();
            if (currentConversationId != -1) {
                loadAdminChat();
            }
            updateUnreadCount();
        } else {
            loadUserChat();
        }
    }
    
    /**
     * Delete conversation (admin only)
     */
    @FXML
    private void deleteConversation() {
        if (currentConversationId == -1) {
            return;
        }
        
        Alert confirm = StyledAlert.createConfirmation(
            "Delete Conversation",
            "Delete this entire conversation?",
            "This will delete all messages and cannot be undone."
        );
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (messageDAO.deleteConversation(currentConversationId)) {
                    currentConversationId = -1;
                    selectedUserId = -1;
                    lblSelectedConversation.setText("Select a conversation");
                    loadConversationList();
                    loadAdminChat();
                    updateUnreadCount();
                }
            }
        });
    }
    
    private void showUserStatus(String message, String type) {
        lblChatStatus.setText(message);
        lblChatStatus.setStyle("-fx-text-fill: " + ("error".equals(type) ? "#f44336" : "#4CAF50") + 
                              "; -fx-font-size: 13px;");
        
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> lblChatStatus.setText(""));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private String formatTime(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            // Today - show time only
            return timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (timestamp.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            // Yesterday
            return "Yesterday " + timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else {
            // Older - show date and time
            return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a"));
        }
    }
}
