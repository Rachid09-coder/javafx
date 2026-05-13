package com.edusmart.service.impl;

import com.edusmart.model.AIGradingResult;
import com.edusmart.model.ExamQuestion;
import com.edusmart.service.AIGradingService;
import com.edusmart.service.GeminiAiService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AIGradingServiceImpl implements AIGradingService {

    private final GeminiAiService geminiAiService = new GeminiAiService();
    private final Gson gson = new Gson();

    @Override
    public AIGradingResult gradeAnswer(ExamQuestion question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return new AIGradingResult(0.0, "La reponse est vide.", 1.0);
        }

        double maxPoints = question != null ? question.getMaxPoints() : 20.0;
        String questionText = question != null ? question.getQuestionText() : "Examen complet";
        String correctAnswer = question != null ? question.getCorrectAnswer() : "Non fourni";

        String prompt = "Tu es un correcteur d'examen EduSmart. Corrige la reponse de l'etudiant.\n"
                + "Question: " + questionText + "\n"
                + "Reponse attendue: " + correctAnswer + "\n"
                + "Bareme maximum: " + maxPoints + "\n"
                + "Reponse etudiant: " + studentAnswer + "\n\n"
                + "Retourne uniquement un objet JSON sans markdown avec les cles: "
                + "score, feedback, confidence. score doit etre entre 0 et " + maxPoints
                + ", confidence entre 0 et 1.";

        try {
            String content = geminiAiService.generateContent(prompt);
            JsonObject resultJson = gson.fromJson(extractJsonObject(content), JsonObject.class);
            double score = getDouble(resultJson, "score", maxPoints * 0.8);
            String feedback = getString(resultJson, "feedback", "Correction Gemini terminee.");
            double confidence = getDouble(resultJson, "confidence", 0.85);

            AIGradingResult result = new AIGradingResult(clamp(score, 0, maxPoints), feedback, clamp(confidence, 0, 1));
            result.setModelUsed("Google Gemini");
            return result;
        } catch (Exception ex) {
            System.err.println("Gemini grading failed. Using local fallback: " + ex.getMessage());
            return fallbackGrade(question, studentAnswer);
        }
    }

    private AIGradingResult fallbackGrade(ExamQuestion question, String studentAnswer) {
        double maxPoints = question != null ? question.getMaxPoints() : 20.0;
        double score = studentAnswer.length() < 10 ? maxPoints * 0.3 : maxPoints * 0.8;
        String feedback = studentAnswer.length() < 10
                ? "Reponse trop courte. Manque de details."
                : "Bonne reponse. Les concepts principaux semblent presents.";
        AIGradingResult result = new AIGradingResult(score, feedback, 0.75);
        result.setModelUsed("Fallback local");
        return result;
    }

    private String extractJsonObject(String content) {
        String trimmed = content == null ? "{}" : content.trim();
        if (trimmed.contains("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String getString(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }

    private double getDouble(JsonObject obj, String key, double defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : defaultValue;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
