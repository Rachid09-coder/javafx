package com.edusmart.service.impl;

import com.edusmart.model.ExamQuestion;
import com.edusmart.service.AIExamService;
import com.edusmart.service.GeminiAiService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates exam QCM questions through the shared Gemini client.
 */
public class AIExamServiceImpl implements AIExamService {

    private final GeminiAiService geminiAiService = new GeminiAiService();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<List<ExamQuestion>> generateQCM(String subject, int count) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String content = geminiAiService.generateQuiz(subject, count, "Moyen", "General");
                return parseQuestions(content);
            } catch (Exception e) {
                e.printStackTrace();
                return generateMockQuestions(subject, count);
            }
        });
    }

    private List<ExamQuestion> parseQuestions(String content) {
        String json = extractJsonArray(content);
        JsonArray array = gson.fromJson(json, JsonArray.class);

        List<ExamQuestion> questions = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            ExamQuestion question = new ExamQuestion();
            question.setQuestionText(getString(obj, "question_text", getString(obj, "question", "")));
            question.setQuestionType(ExamQuestion.QuestionType.MCQ);
            question.setOptions(parseOptions(obj));
            question.setCorrectAnswer(getString(obj, "correct_answer", getString(obj, "correct", "")));
            question.setMaxPoints(getDouble(obj, "max_points", 2.0));
            question.setOrderIndex(i);
            questions.add(question);
        }
        return questions;
    }

    private String extractJsonArray(String content) {
        String trimmed = content == null ? "[]" : content.trim();
        if (trimmed.contains("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String parseOptions(JsonObject obj) {
        if (!obj.has("options") || obj.get("options").isJsonNull()) {
            return "Option A|Option B|Option C|Option D";
        }
        if (obj.get("options").isJsonArray()) {
            List<String> options = new ArrayList<>();
            obj.getAsJsonArray("options").forEach(item -> options.add(item.getAsString()));
            return String.join("|", options);
        }
        return obj.get("options").getAsString();
    }

    private String getString(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }

    private double getDouble(JsonObject obj, String key, double defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : defaultValue;
    }

    private List<ExamQuestion> generateMockQuestions(String subject, int count) {
        List<ExamQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ExamQuestion q = new ExamQuestion();
            q.setQuestionText("[MOCK] Question " + i + " sur " + subject + " ?");
            q.setQuestionType(ExamQuestion.QuestionType.MCQ);
            q.setOptions("Option A|Option B|Option C|Option D");
            q.setCorrectAnswer("Option A");
            q.setMaxPoints(2.0);
            q.setOrderIndex(i - 1);
            questions.add(q);
        }
        return questions;
    }
}
