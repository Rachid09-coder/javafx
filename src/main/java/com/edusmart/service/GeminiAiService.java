package com.edusmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service to interact with Google Gemini AI API for student performance
 * analysis.
 */
public class GeminiAiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    // Si la clé est révoquée, le service utilisera des données simulées.
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";

    private final HttpClient client;
    private final Gson gson;

    public GeminiAiService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    /**
     * General method to generate content via Gemini
     */
    public String generateContent(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", prompt);
        parts.add(partObj);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        requestBody.add("contents", contents);

        String bodyJson = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + API_KEY))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode != 200) {
                // Fallback to simulated response if API key is invalid or quota exceeded
                if (statusCode == 403 || statusCode == 400 || statusCode == 429) {
                    System.err.println("API Key issue or quota exceeded. Using simulated AI response.");
                    return getSimulatedResponse(prompt);
                }
                throw new IOException("Unexpected code " + statusCode + " | " + response.body());
            }

            String responseData = response.body();
            JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);

            // Extract the generated text
            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    /**
     * Extracts student performance insights from their grades.
     */
    public String analyzeStudentPerformance(String studentName, String gradesJson) throws IOException {
        String prompt = "En tant qu'analyste académique expert, analyse les performances de l'étudiant " + studentName +
                " basées sur ces notes (JSON): " + gradesJson +
                ". Fournis une analyse détaillée des points forts, des points faibles et de la progression globale." +
                " Réponds en français de manière professionnelle.";
        return generateContent(prompt);
    }

    /**
     * Generates personalized recommendations for a student.
     */
    public String generateRecommendations(String studentName, String contextJson) throws IOException {
        String prompt = "Basé sur les résultats académiques de " + studentName +
                " (" + contextJson + "), suggère 3 à 5 actions concrètes (exercices, ressources, méthodes) " +
                "pour améliorer ses résultats. Sois encourageant et précis. Réponds en français.";
        return generateContent(prompt);
    }

    /**
     * Analyzes trends for an entire class.
     */
    public String analyzeClassTrends(String className, String classPerformanceJson) throws IOException {
        String prompt = "Analyse les tendances de performance pour la classe " + className +
                " à partir de ces données: " + classPerformanceJson +
                ". Identifie les sujets mal maîtrisés par la majorité et ceux où ils excellent. " +
                "Propose des ajustements pédagogiques. Réponds en français.";
        return generateContent(prompt);
    }

    /**
     * Simulated responses for when the API is unavailable.
     */
    private String getSimulatedResponse(String prompt) {
        if (prompt.contains("tendances de performance pour la classe")) {
            return "🎯 **Analyse (Simulée) de la Classe**\n\n" +
                    "Les données indiquent une excellente maîtrise globale en Mathématiques et Sciences. " +
                    "Cependant, une faiblesse générale est observée en Histoire-Géographie (moyenne de 10.5/20).\n\n" +
                    "**Recommandations Pédagogiques :**\n" +
                    "- 📘 **Histoire-Géo :** Intégrer plus de supports visuels (cartes interactives, documentaires).\n"
                    +
                    "- 🤝 **Travail de groupe :** Organiser des sessions de tutorat où les meilleurs élèves aident ceux en difficulté.\n"
                    +
                    "- ✅ **Évaluations :** Prévoir des mini-quiz réguliers pour consolider la mémorisation.";
        } else if (prompt.contains("performances de l'étudiant")) {
            return "📊 **Analyse Individuelle (Simulée)**\n\n" +
                    "L'étudiant montre une progression stable ce semestre. Ses points forts se situent dans les matières scientifiques, "
                    +
                    "mais il nécessite un soutien supplémentaire en langues vivantes.\n\n" +
                    "**Action suggérée :** Proposer des exercices de compréhension orale.";
        } else {
            return "🤖 **Assistant IA (Mode Hors-Ligne)**\n\n" +
                    "Je suis actuellement en mode simulation car la clé API Google Gemini est invalide ou expirée.\n" +
                    "Cependant, je peux vous confirmer que la classe maintient un bon niveau d'engagement ce mois-ci. "
                    +
                    "Pensez à encourager la participation orale lors des prochaines sessions !";
        }
    }
}
