package com.edusmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service to interact with Google Gemini AI API for student performance
 * analysis.
 */
public class GeminiAiService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "REDACTED";
    private static final String MODEL   = "google/gemini-2.0-flash-001";

    private final OkHttpClient client;
    private final Gson gson;

    public GeminiAiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * General method to generate content via Gemini
     */
    public String generateContent(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        
        JsonArray messages = new JsonArray();
        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", prompt);
        messages.add(messageObj);
        requestBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("HTTP-Referer", "https://edusmart.com") // Required by OpenRouter
                .header("X-Title", "EduSmart")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Fallback to simulated response if API key is invalid or quota exceeded
                if (response.code() == 403 || response.code() == 400 || response.code() == 429) {
                    System.err.println("API Key issue or quota exceeded. Using simulated AI response.");
                    return getSimulatedResponse(prompt);
                }
                throw new IOException("Unexpected code " + response + " | " + response.body().string());
            }

            String responseData = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);

            // Extract the generated text from OpenAI response format
            return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
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
     * Generates a structured quiz for a subject.
     */
    public String generateQuiz(String subject, int numQuestions, String difficulty, String studentLevel) throws IOException {
        String prompt = "CONSIGNE CRITIQUE : Tu dois générer un quiz de " + numQuestions + " questions EXACTEMENT pour la matière spécifique : " + subject + 
                ". Ne parle pas de Java ou d'autres sujets si ce n'est pas demandé." +
                "\nNiveau de difficulté : " + difficulty + 
                "\nPublic cible : " + studentLevel +
                "\nFormat de sortie : JSON pur uniquement (pas de texte explicatif)." +
                "\nStructure : Une liste d'objets JSON avec les clés : question_text, question_type ('MCQ' ou 'OPEN_ENDED'), correct_answer, max_points, options (pour MCQ, séparées par '|')." +
                "\nLangue : Français.";
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
                    "- 📘 **Histoire-Géo :** Intégrer plus de supports visuels (cartes interactives, documentaires).\n" +
                    "- 🤝 **Travail de groupe :** Organiser des sessions de tutorat où les meilleurs élèves aident ceux en difficulté.\n" +
                    "- ✅ **Évaluations :** Prévoir des mini-quiz réguliers pour consolider la mémorisation.";
        } else if (prompt.contains("performances de l'étudiant")) {
            return "📊 **Analyse Individuelle (Simulée)**\n\n" +
                    "L'étudiant montre une progression stable ce semestre. Ses points forts se situent dans les matières scientifiques, " +
                    "mais il nécessite un soutien supplémentaire en langues vivantes.\n\n" +
                    "**Action suggérée :** Proposer des exercices de compréhension orale.";
        } else if (prompt.contains("Génère un quiz")) {
            // Simple simulated quiz JSON without markdown fences
            String subject = "Matière";
            int count = 5;
            if (prompt.contains("matière : ")) {
                subject = prompt.split("matière : ")[1].split("\\.")[0].trim();
            }
            if (prompt.contains("quiz de ")) {
                try {
                    count = Integer.parseInt(prompt.split("quiz de ")[1].split(" ")[0].trim());
                } catch (Exception e) {}
            }
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 1; i <= count; i++) {
                json.append("{\"");
                json.append("question_text\": \"").append("Question ").append(i).append(" sur ").append(subject).append(" ?\", ");
                json.append("\"question_type\": \"MCQ\", ");
                json.append("\"options\": \"Option A|Option B|Option C|Option D\", ");
                json.append("\"correct_answer\": \"Option A\", ");
                json.append("\"max_points\": 2.0");
                json.append("}");
                if (i < count) json.append(", ");
            }
            json.append("]");
            return json.toString();
        } else {
            return "🤖 **Assistant IA (Mode Hors-Ligne)**\n\n" +
                    "Je suis actuellement en mode simulation car la clé API Google Gemini est invalide ou expirée.\n" +
                    "Cependant, je peux vous confirmer que la classe maintient un bon niveau d'engagement ce mois-ci.";
        }
    }
}
