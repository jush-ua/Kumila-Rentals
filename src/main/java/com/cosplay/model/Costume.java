package com.cosplay.model;

public class Costume {
    private int id;
    private String name;
    private String category;
    private String size;
    private String description;
    private String imagePath;

    public Costume() {}

    public Costume(String name, String category, String size, String description, String imagePath) {
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

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, name, size, category);
    }
}
