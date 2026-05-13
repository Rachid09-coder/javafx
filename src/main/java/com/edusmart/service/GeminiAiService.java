package com.edusmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Shared OpenRouter AI client for all EduSmart AI features.
 *
 * Key lookup order:
 * 1. JVM property: -Dopenrouter.api.key=...
 * 2. Environment: OPENROUTER_API_KEY
 * 3. Local ignored file: local-ai.properties
 */
public class GeminiAiService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "openai/gpt-4o-mini";
    private static final String DEFAULT_API_KEY = null;

    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String model;

    public GeminiAiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.apiKey = getConfiguredValue("OPENROUTER_API_KEY", "openrouter.api.key", DEFAULT_API_KEY);
        this.model = getConfiguredValue("OPENROUTER_MODEL", "openrouter.model", DEFAULT_MODEL);
    }

    public String generateContent(String prompt) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OpenRouter API key missing. Using simulated AI response.");
            return getSimulatedResponse(prompt);
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("temperature", 0.7);

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);
        requestBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "https://edusmart.local")
                .header("X-Title", "EduSmart")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                throw new IOException("OpenRouter API error " + response.code() + ": " + extractApiError(responseBody));
            }

            String responseData = response.body() != null ? response.body().string() : "";
            JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IOException("OpenRouter returned no choices.");
            }

            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject responseMessage = firstChoice.getAsJsonObject("message");
            if (responseMessage == null || !responseMessage.has("content")) {
                throw new IOException("OpenRouter returned an empty message.");
            }

            return responseMessage.get("content").getAsString();
        }
    }

    private String extractApiError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response from OpenRouter";
        }

        try {
            JsonObject root = gson.fromJson(responseBody, JsonObject.class);
            if (root != null && root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
            // Fall back to the raw response below.
        }

        String compact = responseBody.replaceAll("\\s+", " ").trim();
        return compact.length() > 300 ? compact.substring(0, 300) + "..." : compact;
    }

    public String analyzeStudentPerformance(String studentName, String gradesJson) throws IOException {
        String prompt = "En tant qu'analyste academique expert, analyse les performances de l'etudiant "
                + studentName + " basees sur ces notes JSON: " + gradesJson
                + ". Fournis une analyse detaillee des points forts, points faibles et de la progression globale. "
                + "Reponds en francais de maniere professionnelle.";
        return generateContent(prompt);
    }

    public String generateRecommendations(String studentName, String contextJson) throws IOException {
        String prompt = "Base sur les resultats academiques de " + studentName + " (" + contextJson
                + "), suggere 3 a 5 actions concretes pour ameliorer ses resultats. "
                + "Sois encourageant et precis. Reponds en francais.";
        return generateContent(prompt);
    }

    public String generateQuiz(String subject, int numQuestions, String difficulty, String studentLevel) throws IOException {
        String prompt = "Genere exactement " + numQuestions + " questions QCM pour la matiere: " + subject + ".\n"
                + "Difficulte: " + difficulty + "\n"
                + "Niveau: " + studentLevel + "\n"
                + "Retourne uniquement un tableau JSON, sans markdown, avec ces cles: "
                + "question_text, question_type, correct_answer, max_points, options. "
                + "question_type doit etre MCQ. options doit contenir les choix separes par |. Langue: francais.";
        return generateContent(prompt);
    }

    public String analyzeClassTrends(String className, String classPerformanceJson) throws IOException {
        String prompt = "Analyse les tendances de performance pour la classe " + className
                + " a partir de ces donnees: " + classPerformanceJson
                + ". Identifie les sujets mal maitrises et propose des ajustements pedagogiques. Reponds en francais.";
        return generateContent(prompt);
    }

    private static String getConfiguredValue(String envName, String propertyName, String defaultValue) {
        String systemValue = System.getProperty(propertyName);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        Path localConfig = Path.of("local-ai.properties");
        if (Files.isRegularFile(localConfig)) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(localConfig.toFile())) {
                props.load(in);
                String localValue = props.getProperty(propertyName);
                if (localValue != null && !localValue.isBlank()) {
                    return localValue.trim();
                }
            } catch (IOException ex) {
                System.err.println("Could not read local AI config: " + ex.getMessage());
            }
        }

        return defaultValue;
    }

    private String getSimulatedResponse(String prompt) {
        if (prompt.contains("tableau JSON") || prompt.contains("questions QCM")) {
            return "["
                    + "{\"question_text\":\"Question 1 sur le sujet demande ?\",\"question_type\":\"MCQ\","
                    + "\"options\":\"Option A|Option B|Option C|Option D\",\"correct_answer\":\"Option A\",\"max_points\":2.0},"
                    + "{\"question_text\":\"Question 2 sur le sujet demande ?\",\"question_type\":\"MCQ\","
                    + "\"options\":\"Option A|Option B|Option C|Option D\",\"correct_answer\":\"Option A\",\"max_points\":2.0}"
                    + "]";
        }

        return "OpenRouter est configure dans l'application, mais la reponse IA n'a pas pu etre recuperee pour le moment.";
    }
}
