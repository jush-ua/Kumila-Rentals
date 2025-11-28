package com.cosplay.model;

public class FeaturedItem {
    private int slot; // 1..4
    private String imageUrl; // could be http(s) or file path
    private String title;
    private Integer cosplayId; // optional: link to a cosplay

    public FeaturedItem() {}

    public FeaturedItem(int slot, String imageUrl, String title) {
        this.slot = slot;
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getCosplayId() { return cosplayId; }
    public void setCosplayId(Integer cosplayId) { this.cosplayId = cosplayId; }
}

