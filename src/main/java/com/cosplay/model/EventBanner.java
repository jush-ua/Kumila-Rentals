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
    private String imagePath;
    private String subtitle;
    private String eventName;
    private String venue;
    private String onsiteRentDate;
    private String titleColor;
    private String subtitleColor;
    
    public EventBanner() {
        this.isActive = false;
        this.backgroundColor = "#fff4ed";
        this.textColor = "#d47f47";
        this.titleColor = "#FFFFFF";
        this.subtitleColor = "#FFFFFF";
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
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public String getOnsiteRentDate() {
        return onsiteRentDate;
    }
    
    public void setOnsiteRentDate(String onsiteRentDate) {
        this.onsiteRentDate = onsiteRentDate;
    }
    
    public String getTitleColor() {
        return titleColor;
    }
    
    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }
    
    public String getSubtitleColor() {
        return subtitleColor;
    }
    
    public void setSubtitleColor(String subtitleColor) {
        this.subtitleColor = subtitleColor;
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
