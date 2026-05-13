package com.edusmart.model;

/**
 * Encapsulates the response from the AI grading service.
 */
public class AIGradingResult {
    private double suggestedScore;
    private String feedback;
    private double confidence;
    private String modelUsed;

    public AIGradingResult(double suggestedScore, String feedback, double confidence) {
        this.suggestedScore = suggestedScore;
        this.feedback = feedback;
        this.confidence = confidence;
    }

    public double getSuggestedScore() { return suggestedScore; }
    public String getFeedback() { return feedback; }
    public double getConfidence() { return confidence; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
}
