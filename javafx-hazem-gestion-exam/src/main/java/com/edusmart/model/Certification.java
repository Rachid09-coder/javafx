package com.edusmart.model;

import java.time.LocalDate;

/**
 * Certification model - represents a student certification/achievement.
 */
public class Certification {

    public enum Status {
        PENDING, ISSUED, EXPIRED, REVOKED
    }

    private int id;
    private int studentId;
    private int courseId;
    private String title;
    private String description;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Status status;
    private String certificateUrl;

    public Certification() {}

    public Certification(int id, int studentId, int courseId, String title, LocalDate issueDate, Status status) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.title = title;
        this.issueDate = issueDate;
        this.status = status;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }

    @Override
    public String toString() {
        return "Certification{id=" + id + ", title='" + title + "', studentId=" + studentId + "}";
    }
}
