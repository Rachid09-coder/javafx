package com.edusmart.model;

/**
 * Value object returned by the AI grading service for a single student answer.
 * Contains estimated score, detailed feedback, and a confidence level.
 */
public class AIGradingResult {

    private double estimatedScore;
    private double maxScore;
    private String feedback;
    private String strengths;
    private String improvements;
    /** AI confidence in this assessment, range 0.0–1.0. */
    private double confidenceLevel;
    private boolean success;
    private String errorMessage;

    public AIGradingResult() {}

    /** Convenience factory for failed/error results. */
    public static AIGradingResult error(String message) {
        AIGradingResult r = new AIGradingResult();
        r.success = false;
        r.errorMessage = message;
        return r;
    }

    /** Convenience factory for successful results. */
    public static AIGradingResult of(double estimatedScore, double maxScore,
                                     String feedback, String strengths,
                                     String improvements, double confidence) {
        AIGradingResult r = new AIGradingResult();
        r.success = true;
        r.estimatedScore = estimatedScore;
        r.maxScore = maxScore;
        r.feedback = feedback;
        r.strengths = strengths;
        r.improvements = improvements;
        r.confidenceLevel = confidence;
        return r;
    }

    public double getEstimatedScore() { return estimatedScore; }
    public void setEstimatedScore(double estimatedScore) { this.estimatedScore = estimatedScore; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }

    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /** Returns percentage score (0–100). */
    public double getScorePercentage() {
        return maxScore > 0 ? (estimatedScore / maxScore) * 100.0 : 0.0;
    }

    @Override
    public String toString() {
        if (!success) return "AIGradingResult{ERROR: " + errorMessage + "}";
        return String.format("AIGradingResult{score=%.1f/%.1f, confidence=%.0f%%}",
                estimatedScore, maxScore, confidenceLevel * 100);
    }
}
