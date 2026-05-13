package com.edusmart.model;

/**
 * Grade model - represents a student's grade for a subject/course (used in Bulletin).
 */
public class Grade {

    private int id;
    private int studentId;
    private int courseId;
    private String subject;
    private double score;
    private double maxScore;
    private String semester;
    private String academicYear;
    private String comment;

    public Grade() {}

    public Grade(int id, int studentId, int courseId, String subject, double score, double maxScore, String semester) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.subject = subject;
        this.score = score;
        this.maxScore = maxScore;
        this.semester = semester;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public double getPercentage() {
        if (maxScore == 0) return 0;
        return (score / maxScore) * 100;
    }

    @Override
    public String toString() {
        return "Grade{studentId=" + studentId + ", subject='" + subject + "', score=" + score + "/" + maxScore + "}";
    }
}
