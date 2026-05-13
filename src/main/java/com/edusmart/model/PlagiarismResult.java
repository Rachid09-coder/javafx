package com.edusmart.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a plagiarism check on a student submission.
 */
public class PlagiarismResult {
    private double similarityScore; // 0.0 to 1.0
    private String summary;
    private List<Match> matches = new ArrayList<>();

    public PlagiarismResult(double similarityScore, String summary) {
        this.similarityScore = similarityScore;
        this.summary = summary;
    }

    public double getSimilarityScore() { return similarityScore; }
    public String getSummary() { return summary; }
    public List<Match> getMatches() { return matches; }

    public static class Match {
        private String source;
        private double overlapPercentage;
        private String snippet;

        public Match(String source, double overlapPercentage, String snippet) {
            this.source = source;
            this.overlapPercentage = overlapPercentage;
            this.snippet = snippet;
        }

        public String getSource() { return source; }
        public double getOverlapPercentage() { return overlapPercentage; }
        public String getSnippet() { return snippet; }
    }
}
