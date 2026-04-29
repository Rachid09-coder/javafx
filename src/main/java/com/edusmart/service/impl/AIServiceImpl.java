package com.edusmart.service.impl;

import com.edusmart.service.AIService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIServiceImpl implements AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openai/gpt-4o-mini";
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executor;

    public AIServiceImpl() {
        this.apiKey = "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";
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.gson = new Gson();
        // Use a cached thread pool to avoid blocking the UI
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<String> askAI(String userMessage, String courseContext) {
        return CompletableFuture.supplyAsync(() -> {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return "⚠️ La clé API OpenAI (OPENAI_API_KEY) est manquante dans les variables d'environnement.";
            }

            try {
                // Build the JSON payload
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", "Tu es un assistant pédagogique expert d'EduSmart. " +
                        "Aide l'étudiant à comprendre le cours avec des explications claires et concises. Ne mets pas de markdown complexe si possible, garde le texte propre. " +
                        "Contexte du cours actuel : " + courseContext);

                JsonObject userMessageObj = new JsonObject();
                userMessageObj.addProperty("role", "user");
                userMessageObj.addProperty("content", userMessage);

                JsonArray messagesArray = new JsonArray();
                messagesArray.add(systemMessage);
                messagesArray.add(userMessageObj);

                JsonObject payloadObj = new JsonObject();
                payloadObj.addProperty("model", MODEL);
                payloadObj.add("messages", messagesArray);
                payloadObj.addProperty("temperature", 0.7);

                String requestBody = gson.toJson(payloadObj);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject responseObj = gson.fromJson(response.body(), JsonObject.class);
                    return responseObj.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString().trim();
                } else {
                    return "❌ Erreur API: " + response.statusCode() + " - " + response.body();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "❌ Une erreur s'est produite lors de la connexion à l'assistant AI: " + e.getMessage();
            }
        }, executor);
    }
}
