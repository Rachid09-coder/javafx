package com.edusmart.model;

import java.time.LocalDate;

/**
 * Exam model - represents an exam or evaluation in EduSmart.
 */
public class Exam {

    public enum Status {
        UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    private int id;
    private String title;
    private String subject;
    private LocalDate date;
    private int durationMinutes;
    private Status status;
    private double maxScore;
    private double studentScore;
    private String description;
    private int courseId;

    public Exam() {}

    public Exam(int id, String title, String subject, LocalDate date, int durationMinutes, Status status) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getStudentScore() { return studentScore; }
    public void setStudentScore(double studentScore) { this.studentScore = studentScore; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    @Override
    public String toString() {
        return "Exam{id=" + id + ", title='" + title + "', subject='" + subject + "', date=" + date + "}";
    }
}
