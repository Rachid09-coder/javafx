package com.edusmart.model;

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
    private String instructor;
    private int moduleCount;
    private int totalHours;
    private Status status;
    private String imageUrl;
    private double price;
    private String category;

    public Course() {}

    public Course(int id, String title, String instructor, int moduleCount, int totalHours, Status status) {
        this.id = id;
        this.title = title;
        this.instructor = instructor;
        this.moduleCount = moduleCount;
        this.totalHours = totalHours;
        this.status = status;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public int getModuleCount() { return moduleCount; }
    public void setModuleCount(int moduleCount) { this.moduleCount = moduleCount; }

    public int getTotalHours() { return totalHours; }
    public void setTotalHours(int totalHours) { this.totalHours = totalHours; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Course{id=" + id + ", title='" + title + "', instructor='" + instructor + "'}";
    }
}
