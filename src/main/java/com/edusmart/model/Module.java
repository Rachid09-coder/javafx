package com.edusmart.model;

import java.time.LocalDate;

/**
 * Module model - represents a module inside a Course.
 */
public class Module {

    private int id;
    private String title;
    private String description;
    private int courseId;
    private int orderIndex;
    private int durationHours;
    private LocalDate createdAt;

    public Module() {}

    public Module(int id, String title, int courseId, int orderIndex, int durationHours) {
        this.id = id;
        this.title = title;
        this.courseId = courseId;
        this.orderIndex = orderIndex;
        this.durationHours = durationHours;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Module{id=" + id + ", title='" + title + "', courseId=" + courseId + "}";
    }
}
