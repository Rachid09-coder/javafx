package com.edusmart.model;

/**
 * Wrapper for submissions to display in a TableView.
 */
public class StudentSubmission {
    private String studentName;
    private String status;
    private Double score;
    private Double plagiarismScore;
    private int submissionId;

    public StudentSubmission(int submissionId, String studentName, String status, Double score, Double plagiarismScore) {
        this.submissionId = submissionId;
        this.studentName = studentName;
        this.status = status;
        this.score = score;
        this.plagiarismScore = plagiarismScore;
    }

    public String getStudentName() { return studentName; }
    public String getStatus() { return status; }
    public Double getScore() { return score; }
    public Double getPlagiarismScore() { return plagiarismScore; }
    public int getSubmissionId() { return submissionId; }
}
