package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Represents a student's answer submission for a single exam question.
 * Tracks AI grading result, plagiarism score, and manual override.
 */
public class ExamSubmission {

    public enum SubmissionStatus {
        PENDING, SUBMITTED, AI_GRADED, MANUALLY_GRADED, PLAGIARISM_FLAGGED, FAILED
    }

    private int id;
    private int examId;
    private int studentId;
    /** Transient – populated via JOIN with user table. */
    private String studentName;
    private String studentEmail;
    private int questionId;
    private String studentAnswer;
    private double score;
    private double maxScore;
    private String aiFeedback;
    /** AI confidence in grading, range 0.0–1.0. */
    private double aiConfidence;
    /** Plagiarism similarity score, range 0.0–100.0. */
    private double plagiarismScore;
    private String filePath;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    public ExamSubmission() {}

    // ── Getters & Setters ────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(double aiConfidence) { this.aiConfidence = aiConfidence; }

    public double getPlagiarismScore() { return plagiarismScore; }
    public void setPlagiarismScore(double plagiarismScore) { this.plagiarismScore = plagiarismScore; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    /** Computed percentage score (0–100). */
    public double getScorePercentage() {
        return maxScore > 0 ? (score / maxScore) * 100.0 : 0.0;
    }

    @Override
    public String toString() {
        return "ExamSubmission{studentId=" + studentId + ", questionId=" + questionId
                + ", score=" + score + "/" + maxScore + ", status=" + status + "}";
    }
}
