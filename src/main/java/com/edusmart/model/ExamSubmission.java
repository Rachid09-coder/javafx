package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Represents a student's answer to a specific exam question.
 */
public class ExamSubmission {

    public enum Status {
        SUBMITTED, GRADED, PENDING
    }

    private int id;
    private int studentId;
    private int examId;
    private int questionId;
    private String studentAnswer;
    private Double score;
    private String teacherFeedback;
    private LocalDateTime submissionDate;
    private Status status = Status.SUBMITTED;
    private Double plagiarismScore;
    private String aiFeedback;

    public ExamSubmission() {
        this.submissionDate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Double getPlagiarismScore() { return plagiarismScore; }
    public void setPlagiarismScore(Double plagiarismScore) { this.plagiarismScore = plagiarismScore; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
}
