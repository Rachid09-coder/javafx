package com.edusmart.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a plagiarism analysis for a single student's answer.
 * Automatically flagged when similarity exceeds 70%.
 */
public class PlagiarismResult {

    private int submissionId;
    private int studentId;
    private String studentName;
    private double maxSimilarityScore; // 0.0 – 100.0
    private boolean flagged;
    private List<PlagiarismMatch> matches = new ArrayList<>();

    /** Represents a similarity match between two students. */
    public static class PlagiarismMatch {
        private int comparedStudentId;
        private String comparedStudentName;
        private double similarityPercent;

        public PlagiarismMatch(int comparedStudentId, String comparedStudentName, double similarityPercent) {
            this.comparedStudentId = comparedStudentId;
            this.comparedStudentName = comparedStudentName;
            this.similarityPercent = similarityPercent;
        }

        public int getComparedStudentId() { return comparedStudentId; }
        public String getComparedStudentName() { return comparedStudentName; }
        public double getSimilarityPercent() { return similarityPercent; }

        @Override
        public String toString() {
            return String.format("vs %s: %.1f%%", comparedStudentName, similarityPercent);
        }
    }

    public PlagiarismResult() {}

    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getMaxSimilarityScore() { return maxSimilarityScore; }
    public void setMaxSimilarityScore(double maxSimilarityScore) {
        this.maxSimilarityScore = maxSimilarityScore;
        this.flagged = maxSimilarityScore >= 70.0;
    }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public List<PlagiarismMatch> getMatches() { return matches; }
    public void setMatches(List<PlagiarismMatch> matches) { this.matches = matches; }
    public void addMatch(PlagiarismMatch match) { this.matches.add(match); }
}
