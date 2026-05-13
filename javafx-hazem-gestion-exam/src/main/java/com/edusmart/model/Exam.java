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
    private String description;
    private String type;
    private String filePath;
    private String externalLink;
    private Integer duration;
    private String moduleName;
    private String gradeCategory;
    private String academicYear;
    private Integer semester;
    private Double coefficient;
    private Integer courseIdNullable;

    // Legacy/demo fields kept for compatibility with existing student views.
    private String subject;
    private LocalDate date;
    private int durationMinutes;
    private Status status;
    private double maxScore;
    private double studentScore;
    private int courseId;

    public Exam() {}

    public Exam(int id, String title, String subject, LocalDate date, int durationMinutes, Status status) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.duration = durationMinutes;
        this.moduleName = subject;
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
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
        this.duration = durationMinutes;
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getStudentScore() { return studentScore; }
    public void setStudentScore(double studentScore) { this.studentScore = studentScore; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String externalLink) { this.externalLink = externalLink; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) {
        this.duration = duration;
        this.durationMinutes = duration != null ? duration : 0;
    }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
        this.subject = moduleName;
    }

    public String getGradeCategory() { return gradeCategory; }
    public void setGradeCategory(String gradeCategory) { this.gradeCategory = gradeCategory; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Double getCoefficient() { return coefficient; }
    public void setCoefficient(Double coefficient) { this.coefficient = coefficient; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) {
        this.courseId = courseId;
        this.courseIdNullable = courseId;
    }

    public Integer getCourseIdNullable() { return courseIdNullable; }
    public void setCourseIdNullable(Integer courseIdNullable) {
        this.courseIdNullable = courseIdNullable;
        this.courseId = courseIdNullable != null ? courseIdNullable : 0;
    }

    @Override
    public String toString() {
        return "Exam{id=" + id + ", title='" + title + "', subject='" + subject + "', date=" + date + "}";
    }
}
