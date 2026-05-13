package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Course model - represents an EduSmart course.
 */
public class Course {

    public enum Status {
        ACTIVE, INACTIVE, DRAFT, ARCHIVED
    }

    private int id;
    private String title;
    private String description;
    private double price;
    private String statusValue;
    private LocalDateTime createdAt;
    private String thumbnailPath;
    private String pdfPath;
    private String generatedContent;
    private Double coefficient;
    /** Optional FK to {@code module.id}; null when the course is not tied to a module. */
    private Integer moduleId;

    // Advanced UI features (transient / not persisted by default)
    private double rating = 0.0;
    private boolean favorite = false;

    // Legacy UI fields kept for compatibility with existing JavaFX demo controllers.
    private String instructor;
    private int moduleCount;
    private int totalHours;
    private Status status;
    private String category;
    private String imageUrl;

    public Course() {}

    public Course(int id, String title, String instructor, int moduleCount, int totalHours, Status status) {
        this.id = id;
        this.title = title;
        this.instructor = instructor;
        this.moduleCount = moduleCount;
        this.totalHours = totalHours;
        this.status = status;
        this.statusValue = status != null ? status.name() : null;
    }

    public Course(int id, String title, String description, double price, String statusValue, LocalDateTime createdAt,
                  String thumbnailPath, String pdfPath, String generatedContent, Double coefficient) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.statusValue = statusValue;
        this.createdAt = createdAt;
        this.thumbnailPath = thumbnailPath;
        this.pdfPath = pdfPath;
        this.generatedContent = generatedContent;
        this.coefficient = coefficient;
        this.status = parseStatus(statusValue);
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatusValue() { return statusValue; }
    public void setStatusValue(String statusValue) {
        this.statusValue = statusValue;
        this.status = parseStatus(statusValue);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getGeneratedContent() { return generatedContent; }
    public void setGeneratedContent(String generatedContent) { this.generatedContent = generatedContent; }

    public Double getCoefficient() { return coefficient; }
    public void setCoefficient(Double coefficient) { this.coefficient = coefficient; }

    public Integer getModuleId() { return moduleId; }
    public void setModuleId(Integer moduleId) { this.moduleId = moduleId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public int getModuleCount() { return moduleCount; }
    public void setModuleCount(int moduleCount) { this.moduleCount = moduleCount; }

    public int getTotalHours() { return totalHours; }
    public void setTotalHours(int totalHours) { this.totalHours = totalHours; }

    public Status getStatus() {
        if (status == null && statusValue != null) {
            status = parseStatus(statusValue);
        }
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
        this.statusValue = status != null ? status.name() : null;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    private Status parseStatus(String statusText) {
        if (statusText == null) {
            return null;
        }
        try {
            return Status.valueOf(statusText.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", title='" + title + "', price=" + price + ", status='" + statusValue
                + "', moduleId=" + moduleId + "}";
    }
}
