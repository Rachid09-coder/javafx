package com.edusmart.model;

/**
 * Product model - represents an item in the EduSmart shop.
 */
public class Product {

    public enum Category {
        BOOK, SOFTWARE, COURSE_MATERIAL, EQUIPMENT, OTHER
    }

    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private Category category;
    private String imageUrl;

    public Product() {}

    public Product(int id, String name, double price, int stock, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}
