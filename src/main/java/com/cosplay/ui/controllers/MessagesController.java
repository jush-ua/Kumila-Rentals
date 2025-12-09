package com.cosplay.ui.controllers;

import com.cosplay.dao.MessageDAO;
import com.cosplay.model.Message;
import com.cosplay.model.User;
import com.cosplay.ui.Views;
import com.cosplay.util.EmailUtil;
import com.cosplay.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesController {
    @FXML private NavController navBarController;
    
    // User components
    @FXML private VBox userMessageSection;
    @FXML private VBox userHistorySection;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtMessage;
    @FXML private Button btnSendMessage;
    @FXML private Label lblSendStatus;
    @FXML private VBox userMessagesContainer;
    
    // Admin components
    @FXML private VBox adminMessageSection;
    @FXML private VBox adminMessagesContainer;
    @FXML private Label lblUnreadCount;
    @FXML private ToggleButton btnFilterAll;
    @FXML private ToggleButton btnFilterUnread;
    @FXML private ToggleButton btnFilterRead;
    @FXML private ToggleButton btnFilterReplied;
    
    private MessageDAO messageDAO;
    private User currentUser;
    private boolean isAdmin;
    private String currentFilter = "all";
    private ToggleGroup filterGroup;

    @FXML
    private void initialize() {
        // Set active highlight for nav bar
        if (navBarController != null) {
            navBarController.setActive(Views.MESSAGES);
        }
        
        messageDAO = new MessageDAO();
        currentUser = Session.getCurrentUser();
        isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        
        // Setup filter toggle group
        filterGroup = new ToggleGroup();
        btnFilterAll.setToggleGroup(filterGroup);
        btnFilterUnread.setToggleGroup(filterGroup);
        btnFilterRead.setToggleGroup(filterGroup);
        btnFilterReplied.setToggleGroup(filterGroup);
        
        setupView();
        loadMessages();
        updateUnreadCount();
    }
    
    private void setupView() {
        if (isAdmin) {
            // Show admin view
            userMessageSection.setManaged(false);
            userMessageSection.setVisible(false);
            userHistorySection.setManaged(false);
            userHistorySection.setVisible(false);
            adminMessageSection.setManaged(true);
            adminMessageSection.setVisible(true);
        } else {
            // Show user view
            userMessageSection.setManaged(true);
            userMessageSection.setVisible(true);
            userHistorySection.setManaged(true);
            userHistorySection.setVisible(true);
            adminMessageSection.setManaged(false);
            adminMessageSection.setVisible(false);
        }
    }
    
    @FXML
    private void sendMessage() {
        String subject = txtSubject.getText().trim();
        String messageText = txtMessage.getText().trim();
        
        // Validation
        if (subject.isEmpty()) {
            showStatus("Please enter a subject", "error");
            return;
        }
        
        if (messageText.isEmpty()) {
            showStatus("Please enter a message", "error");
            return;
        }
        
        // Create message
        Message message = new Message();
        message.setSenderId(currentUser.getUserId());
        message.setSenderName(currentUser.getUsername());
        message.setSenderEmail(currentUser.getEmail());
        message.setSubject(subject);
        message.setMessage(messageText);
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("unread");
        
        // Save to database
        if (messageDAO.sendMessage(message)) {
            showStatus("Message sent successfully!", "success");
            
            // Clear form
            txtSubject.clear();
            txtMessage.clear();
            
            // Reload user messages
            loadMessages();
            
            // Optional: Send email notification to admin (disabled for now)
            // notifyAdminByEmail(message);
        } else {
            showStatus("Failed to send message. Please try again.", "error");
        }
    }
    
    @FXML
    private void loadMessages() {
        if (isAdmin) {
            loadAdminMessages();
        } else {
            loadUserMessages();
        }
        updateUnreadCount();
    }
    
    private void loadUserMessages() {
        userMessagesContainer.getChildren().clear();
        List<Message> messages = messageDAO.getMessagesByUser(currentUser.getUserId());
        
        if (messages.isEmpty()) {
            Label noMessages = new Label("No messages yet");
            noMessages.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            userMessagesContainer.getChildren().add(noMessages);
        } else {
            for (Message msg : messages) {
                userMessagesContainer.getChildren().add(createUserMessageCard(msg));
            }
        }
    }
    
    private void loadAdminMessages() {
        adminMessagesContainer.getChildren().clear();
        List<Message> messages = messageDAO.getAllMessages();
        
        // Apply filter
        messages = messages.stream()
            .filter(msg -> {
                switch (currentFilter) {
                    case "unread": return "unread".equals(msg.getStatus());
                    case "read": return "read".equals(msg.getStatus());
                    case "replied": return "replied".equals(msg.getStatus());
                    default: return true;
                }
            })
            .toList();
        
        if (messages.isEmpty()) {
            Label noMessages = new Label("No messages found");
            noMessages.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            adminMessagesContainer.getChildren().add(noMessages);
        } else {
            for (Message msg : messages) {
                adminMessagesContainer.getChildren().add(createAdminMessageCard(msg));
            }
        }
    }
    
    private VBox createUserMessageCard(Message msg) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with subject and status
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label subject = new Label(msg.getSubject());
        subject.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox.setHgrow(subject, Priority.ALWAYS);
        
        Label status = new Label(msg.getStatus().toUpperCase());
        status.setStyle(getStatusStyle(msg.getStatus()));
        
        Label timestamp = new Label(formatTimestamp(msg.getTimestamp()));
        timestamp.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        header.getChildren().addAll(subject, status, timestamp);
        
        // Message content
        Label content = new Label(msg.getMessage());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        
        card.getChildren().addAll(header, content);
        return card;
    }
    
    private VBox createAdminMessageCard(Message msg) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label sender = new Label("From: " + msg.getSenderName() + " (" + msg.getSenderEmail() + ")");
        sender.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        HBox.setHgrow(sender, Priority.ALWAYS);
        
        Label timestamp = new Label(formatTimestamp(msg.getTimestamp()));
        timestamp.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        header.getChildren().addAll(sender, timestamp);
        
        // Subject
        Label subject = new Label(msg.getSubject());
        subject.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Message content
        Label content = new Label(msg.getMessage());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label statusLabel = new Label("Status: " + msg.getStatus().toUpperCase());
        statusLabel.setStyle(getStatusStyle(msg.getStatus()));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        if ("unread".equals(msg.getStatus())) {
            Button markRead = new Button("Mark as Read");
            markRead.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-cursor: hand;");
            markRead.setOnAction(e -> {
                messageDAO.markAsRead(msg.getMessageId());
                loadMessages();
            });
            actions.getChildren().add(markRead);
        }
        
        if (!"replied".equals(msg.getStatus())) {
            Button markReplied = new Button("Mark as Replied");
            markReplied.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-cursor: hand;");
            markReplied.setOnAction(e -> {
                messageDAO.markAsReplied(msg.getMessageId());
                loadMessages();
            });
            actions.getChildren().add(markReplied);
        }
        
        Button delete = new Button("Delete");
        delete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-cursor: hand;");
        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete this message?");
            confirm.setContentText("This action cannot be undone.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    messageDAO.deleteMessage(msg.getMessageId());
                    loadMessages();
                }
            });
        });
        actions.getChildren().add(delete);
        
        actions.getChildren().add(0, statusLabel);
        
        card.getChildren().addAll(header, subject, content, actions);
        return card;
    }
    
    @FXML
    private void filterMessages() {
        if (btnFilterAll.isSelected()) {
            currentFilter = "all";
        } else if (btnFilterUnread.isSelected()) {
            currentFilter = "unread";
        } else if (btnFilterRead.isSelected()) {
            currentFilter = "read";
        } else if (btnFilterReplied.isSelected()) {
            currentFilter = "replied";
        }
        loadMessages();
    }
    
    private void updateUnreadCount() {
        if (isAdmin) {
            int count = messageDAO.getUnreadCount();
            Platform.runLater(() -> lblUnreadCount.setText(count + " unread"));
        }
    }
    
    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "unread" -> "-fx-background-color: #ff5252; -fx-text-fill: white; -fx-padding: 3 8; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "read" -> "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "replied" -> "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 3 8; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;";
            default -> "-fx-background-color: #999; -fx-text-fill: white; -fx-padding: 3 8; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 11px;";
        };
    }
    
    private void showStatus(String message, String type) {
        lblSendStatus.setText(message);
        lblSendStatus.setStyle("-fx-text-fill: " + ("error".equals(type) ? "#f44336" : "#4CAF50") + "; -fx-font-size: 13px;");
        
        // Clear status after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> lblSendStatus.setText(""));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
        return timestamp.format(formatter);
    }
}
