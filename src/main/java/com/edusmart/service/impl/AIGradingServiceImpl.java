package com.edusmart.service.impl;

import com.edusmart.model.AIGradingResult;
import com.edusmart.model.ExamQuestion;
import com.edusmart.model.ExamSubmission;
import com.edusmart.service.AIGradingService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AIGradingServiceImpl — calls OpenAI Chat Completions API asynchronously.
 *
 * Configuration via environment variables:
 *   OPENAI_API_KEY  — your OpenAI secret key (required)
 *   OPENAI_MODEL    — model to use (default: gpt-4o-mini)
 */
public class AIGradingServiceImpl implements AIGradingService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "openai/gpt-4o-mini";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ExecutorService executor;

    public AIGradingServiceImpl() {
        this.apiKey  = System.getenv("OPENROUTER_API_KEY");
        this.model   = System.getenv().getOrDefault("OPENAI_MODEL", DEFAULT_MODEL);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.executor = Executors.newFixedThreadPool(4,
                r -> { Thread t = new Thread(r, "ai-grading"); t.setDaemon(true); return t; });
    }

    @Override
    public CompletableFuture<AIGradingResult> gradeAnswerAsync(ExamQuestion question,
                                                                ExamSubmission submission) {
        return CompletableFuture.supplyAsync(() -> callOpenAI(question, submission), executor);
    }

    @Override
    public CompletableFuture<List<AIGradingResult>> gradeAllAnswersAsync(ExamQuestion question,
                                                                          List<ExamSubmission> submissions) {
        List<CompletableFuture<AIGradingResult>> futures = new ArrayList<>();
        for (ExamSubmission s : submissions) {
            futures.add(gradeAnswerAsync(question, s));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<AIGradingResult> results = new ArrayList<>();
                    for (CompletableFuture<AIGradingResult> f : futures) {
                        results.add(f.join());
                    }
                    return results;
                });
    }

    // ── Private: OpenAI call ─────────────────────────────────────────────

    private AIGradingResult callOpenAI(ExamQuestion question, ExamSubmission submission) {
        if (apiKey == null || apiKey.isBlank()) {
            return simulateGrading(question, submission);
        }
        try {
            String prompt = buildPrompt(question, submission);
            String jsonBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseOpenAIResponse(response.body(), question.getMaxPoints());
            } else {
                System.err.println("OpenAI API error " + response.statusCode() + ": " + response.body());
                return simulateGrading(question, submission);
            }
        } catch (Exception ex) {
            System.err.println("AIGradingService error: " + ex.getMessage());
            return simulateGrading(question, submission);
        }
    }

    private String buildPrompt(ExamQuestion question, ExamSubmission submission) {
        return "You are an expert teacher grading an exam answer.\n\n" +
               "QUESTION: " + question.getQuestionText() + "\n" +
               "CORRECT ANSWER: " + (question.getCorrectAnswer() != null ? question.getCorrectAnswer() : "Open-ended, judge quality.") + "\n" +
               "MAX POINTS: " + question.getMaxPoints() + "\n" +
               "STUDENT ANSWER: " + submission.getStudentAnswer() + "\n\n" +
               "Respond with ONLY this JSON (no extra text):\n" +
               "{\n" +
               "  \"score\": <number 0 to " + question.getMaxPoints() + ">,\n" +
               "  \"confidence\": <number 0.0 to 1.0>,\n" +
               "  \"feedback\": \"<overall feedback>\",\n" +
               "  \"strengths\": \"<what the student did well>\",\n" +
               "  \"improvements\": \"<what could be improved>\"\n" +
               "}";
    }

    private String buildRequestBody(String prompt) {
        // Manual JSON building to avoid external library dependency
        String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
        return "{\n" +
               "  \"model\": \"" + model + "\",\n" +
               "  \"messages\": [{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}],\n" +
               "  \"temperature\": 0.2,\n" +
               "  \"max_tokens\": 400\n" +
               "}";
    }

    private AIGradingResult parseOpenAIResponse(String json, double maxPoints) {
        try {
            // Simple manual JSON field extraction (no external lib)
            double score      = extractDouble(json, "score", 0);
            double confidence = extractDouble(json, "confidence", 0.8);
            String feedback   = extractString(json, "feedback", "Graded by AI.");
            String strengths  = extractString(json, "strengths", "");
            String improvements = extractString(json, "improvements", "");

            // Clamp score to [0, maxPoints]
            score = Math.max(0, Math.min(score, maxPoints));
            return AIGradingResult.of(score, maxPoints, feedback, strengths, improvements, confidence);
        } catch (Exception ex) {
            return AIGradingResult.error("Failed to parse AI response: " + ex.getMessage());
        }
    }

    /** Fallback: simulate AI grading when no API key is configured. */
    private AIGradingResult simulateGrading(ExamQuestion question, ExamSubmission submission) {
        String answer = submission.getStudentAnswer();
        if (answer == null || answer.isBlank()) {
            return AIGradingResult.of(0, question.getMaxPoints(),
                    "No answer provided.", "", "Please provide a complete answer.", 0.99);
        }
        String correct = question.getCorrectAnswer() != null ? question.getCorrectAnswer().toLowerCase() : "";
        String given   = answer.toLowerCase();
        double similarity = correct.isEmpty() ? 0.6 : cosineSimilarity(correct, given);
        double score = Math.round(similarity * question.getMaxPoints() * 10.0) / 10.0;

        String feedback = score >= question.getMaxPoints() * 0.8
                ? "Excellent answer demonstrating strong understanding."
                : score >= question.getMaxPoints() * 0.5
                        ? "Adequate answer but could be more detailed."
                        : "Answer lacks key concepts. Review the material.";

        return AIGradingResult.of(score, question.getMaxPoints(), feedback,
                "Effort is visible.", "Add more specific examples.", similarity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private double cosineSimilarity(String a, String b) {
        String[] wordsA = a.split("\\s+");
        String[] wordsB = b.split("\\s+");
        java.util.Set<String> vocab = new java.util.HashSet<>();
        for (String w : wordsA) vocab.add(w);
        for (String w : wordsB) vocab.add(w);
        String[] terms = vocab.toArray(new String[0]);
        double[] vecA = new double[terms.length];
        double[] vecB = new double[terms.length];
        for (int i = 0; i < terms.length; i++) {
            for (String w : wordsA) if (w.equals(terms[i])) vecA[i]++;
            for (String w : wordsB) if (w.equals(terms[i])) vecB[i]++;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < terms.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double extractDouble(String json, String key, double defaultVal) {
        String marker = "\"" + key + "\"";
        int keyIdx = json.indexOf(marker);
        if (keyIdx < 0) return defaultVal;
        
        int colonIdx = json.indexOf(":", keyIdx + marker.length());
        if (colonIdx < 0) return defaultVal;
        
        int start = colonIdx + 1;
        // skip whitespace and potential quotes
        while (start < json.length() && (Character.isWhitespace(json.charAt(start)) || json.charAt(start) == '"')) start++;
        
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        
        if (start == end) return defaultVal;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private String extractString(String json, String key, String defaultVal) {
        String marker = "\"" + key + "\"";
        int keyIdx = json.indexOf(marker);
        if (keyIdx < 0) return defaultVal;
        
        int colonIdx = json.indexOf(":", keyIdx + marker.length());
        if (colonIdx < 0) return defaultVal;
        
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) return defaultVal;
        
        int endQuote = json.indexOf("\"", startQuote + 1);
        // Handle escaped quotes
        while (endQuote > 0 && json.charAt(endQuote - 1) == '\\') {
            endQuote = json.indexOf("\"", endQuote + 1);
        }
        
        if (endQuote < 0) return defaultVal;
        return json.substring(startQuote + 1, endQuote)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
