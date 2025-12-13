package com.cosplay.model;

public class Cosplay {
    private int id;
    private String name;
    private String category;
    private String seriesName;
    private String size;
    private String description;
    private String imagePath;
    private Double rentRate1Day;
    private Double rentRate2Days;
    private Double rentRate3Days;
    private String addOns; // JSON string or comma-separated list

    public Cosplay() {}

    public Cosplay(String name, String category, String size, String description, String imagePath) {
        this.name = name;
        this.category = category;
        this.size = size;
        this.description = description;
        this.imagePath = imagePath;
    }

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }
    public Double getRentRate1Day() { return rentRate1Day; }
    public void setRentRate1Day(Double rentRate1Day) { this.rentRate1Day = rentRate1Day; }
    public Double getRentRate2Days() { return rentRate2Days; }
    public void setRentRate2Days(Double rentRate2Days) { this.rentRate2Days = rentRate2Days; }
    public Double getRentRate3Days() { return rentRate3Days; }
    public void setRentRate3Days(Double rentRate3Days) { this.rentRate3Days = rentRate3Days; }
    public String getAddOns() { return addOns; }
    public void setAddOns(String addOns) { this.addOns = addOns; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, name, size, category);
    }
}
