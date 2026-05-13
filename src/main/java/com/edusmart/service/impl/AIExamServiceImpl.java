package com.edusmart.service.impl;

import com.edusmart.model.ExamQuestion;
import com.edusmart.service.AIExamService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AIExamServiceImpl - Real implementation calling OpenAI to generate QCM.
 */
public class AIExamServiceImpl implements AIExamService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "openai/gpt-4o-mini";
    
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public AIExamServiceImpl() {
        this.apiKey = System.getenv("OPENROUTER_API_KEY");
        this.model = System.getenv().getOrDefault("OPENAI_MODEL", DEFAULT_MODEL);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public CompletableFuture<List<ExamQuestion>> generateQCM(String subject, int count) {
        if (apiKey == null || apiKey.isBlank()) {
            return generateMockQuestions(subject, count);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = String.format(
                    "Generate %d multiple choice questions (QCM) about %s.\n" +
                    "Each question must have:\n" +
                    "- 1 correct answer\n" +
                    "- 3 wrong answers\n\n" +
                    "Return ONLY a JSON array in this format:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"...\",\n" +
                    "    \"options\": [\"A\", \"B\", \"C\", \"D\"],\n" +
                    "    \"correct\": \"A\"\n" +
                    "  }\n" +
                    "]", count, subject);

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);
                JSONArray messages = new JSONArray();
                messages.put(new JSONObject().put("role", "user").put("content", prompt));
                requestBody.put("messages", messages);
                requestBody.put("temperature", 0.7);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseResponse(response.body());
                } else {
                    System.err.println("OpenAI API error: " + response.body());
                    return generateMockQuestions(subject, count).join();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return generateMockQuestions(subject, count).join();
            }
        });
    }

    private List<ExamQuestion> parseResponse(String responseBody) {
        List<ExamQuestion> questions = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(responseBody);
        String content = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // Clean up markdown if present
        if (content.contains("```json")) {
            content = content.substring(content.indexOf("```json") + 7);
            content = content.substring(0, content.lastIndexOf("```"));
        } else if (content.contains("```")) {
            content = content.substring(content.indexOf("```") + 3);
            content = content.substring(0, content.lastIndexOf("```"));
        }

        JSONArray array = new JSONArray(content.trim());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            ExamQuestion q = new ExamQuestion();
            q.setQuestionText(obj.getString("question"));
            q.setQuestionType(ExamQuestion.QuestionType.MCQ);
            
            JSONArray optionsArray = obj.getJSONArray("options");
            List<String> optionsList = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                optionsList.add(optionsArray.getString(j));
            }
            q.setOptions(String.join("|", optionsList));
            q.setCorrectAnswer(obj.getString("correct"));
            q.setMaxPoints(2.0);
            q.setOrderIndex(i);
            questions.add(q);
        }
        return questions;
    }

    private CompletableFuture<List<ExamQuestion>> generateMockQuestions(String subject, int count) {
        return CompletableFuture.supplyAsync(() -> {
            List<ExamQuestion> questions = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                ExamQuestion q = new ExamQuestion();
                q.setQuestionText("[MOCK] Question " + i + " on " + subject + "?");
                q.setQuestionType(ExamQuestion.QuestionType.MCQ);
                q.setOptions("Option A|Option B|Option C|Option D");
                q.setCorrectAnswer("Option A");
                q.setMaxPoints(2.0);
                q.setOrderIndex(i - 1);
                questions.add(q);
            }
            return questions;
        });
    }
}
