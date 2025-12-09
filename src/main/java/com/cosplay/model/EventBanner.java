package com.cosplay.model;

public class EventBanner {
    private Integer id;
    private String title;
    private String message;
    private boolean isActive;
    private String backgroundColor;
    private String textColor;
    private String linkUrl;
    private String linkText;
    
    public EventBanner() {
        this.isActive = false;
        this.backgroundColor = "#fff4ed";
        this.textColor = "#d47f47";
    }
    
    public EventBanner(String title, String message) {
        this();
        this.title = title;
        this.message = message;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public String getLinkText() {
        return linkText;
    }
    
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }
    
    @Override
    public String toString() {
        return "EventBanner{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
