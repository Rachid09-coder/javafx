package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Module model - represents a module inside a Course.
 */
public class Module {

    private int id;
    private String title;
    private String description;
    private String thumbnail;
    private LocalDateTime createdAt;

    // Legacy UI fields kept for compatibility with existing JavaFX demo controllers.
    private int courseId;
    private int orderIndex;
    private int durationHours;

    public Module() {}

    public Module(int id, String title, int courseId, int orderIndex, int durationHours) {
        this.id = id;
        this.title = title;
        this.courseId = courseId;
        this.orderIndex = orderIndex;
        this.durationHours = durationHours;
    }

    public Module(int id, String title, String description, String thumbnail, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Module{id=" + id + ", title='" + title + "', thumbnail='" + thumbnail + "'}";
    }
}
