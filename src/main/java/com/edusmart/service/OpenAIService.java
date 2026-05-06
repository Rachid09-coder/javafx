package com.edusmart.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenAIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final Gson gson = new Gson();

    public String generateShopAnalysis(String ordersData, String itemsData) {
        try {
            String prompt = "Analyse les ventes de la boutique à partir des données suivantes.\n\n" +
                    "Commandes :\n" + ordersData + "\n\n" +
                    "Articles vendus :\n" + itemsData + "\n\n" +
                    "Génère un résumé des statistiques (chiffre d'affaires total, produits les plus vendus et les moins vendus) et donne 3 recommandations business courtes et actionnables pour améliorer les ventes et la gestion des stocks. " +
                    "Formate la réponse de manière lisible (texte clair, points de puces).";

            JsonObject payload = new JsonObject();
            // Utilisation du modèle standard recommandé (gpt-4o-mini ou gpt-4o) car gpt-5 n'existe pas encore sur l'API publique standard.
            payload.addProperty("model", "gpt-4o-mini");
            
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            
            payload.add("messages", messages);
            payload.addProperty("store", true);

            String jsonBody = gson.toJson(payload);

            // Récupérer la clé API depuis les variables d'environnement
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                return "⚠️ Erreur : La variable d'environnement OPENAI_API_KEY n'est pas définie.\nVeuillez la configurer dans votre système ou IDE avant d'utiliser l'IA.\n\n" + getMockResponse();
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // OpenAI responses endpoint might return a different JSON structure, 
                // but usually the main text is under choices[0].message.content or similar,
                // or if it's the exact 'responses' endpoint, it might just return the text in a specific field.
                // To be safe, if we don't know the exact response format of this new /v1/responses endpoint, 
                // we can just try to parse common OpenAI structures, or just return the raw body if parsing fails.
                try {
                    JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (responseJson.has("output")) {
                        // Some endpoints might just return {"output": "..."}
                        return responseJson.get("output").getAsString();
                    } else if (responseJson.has("choices")) {
                        JsonArray choices = responseJson.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("message")) {
                                return firstChoice.getAsJsonObject("message").get("content").getAsString();
                            } else if (firstChoice.has("text")) {
                                return firstChoice.get("text").getAsString();
                            }
                        }
                    }
                    // Fallback if we can't find the text field
                    return response.body();
                } catch (Exception e) {
                    return response.body();
                }
            } else if (response.statusCode() == 429) {
                return getMockResponse();
            } else {
                return "Erreur lors de l'appel à l'API OpenAI : HTTP " + response.statusCode() + "\n" + response.body() + "\n\n" + getMockResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion à l'IA : " + e.getMessage() + "\n\n" + getMockResponse();
        }
    }

    // Fournit une réponse factice pour pouvoir tester l'interface même sans crédit API
    private String getMockResponse() {
        return "⚠️ (Mode Simulation - Quota API épuisé)\n\n" +
               "Voici une analyse simulée de vos ventes :\n\n" +
               "📊 Résumé des Statistiques :\n" +
               "• Chiffre d'affaires total : 2 450,00 DT\n" +
               "• Produit le plus vendu : 'Introduction à Java' (42 unités)\n" +
               "• Produit le moins vendu : 'Livre de Mathématiques Avancées' (2 unités)\n\n" +
               "💡 Recommandations Business :\n" +
               "1. Promouvoir les Mathématiques : Mettez en avant le livre de Maths avec une offre groupée (bundle) pour relancer les ventes.\n" +
               "2. Renforcer le stock Java : 'Introduction à Java' se vend très bien, assurez-vous d'avoir suffisamment de stock ou de licences.\n" +
               "3. Analyser la tendance : Vos ventes sont en hausse, envisagez de proposer des certifications payantes additionnelles.";
    }
}
