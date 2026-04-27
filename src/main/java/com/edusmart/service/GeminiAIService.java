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

public class GeminiAIService {
    private static final String API_KEY = "AIzaSyDIUjq_7nPCKXB-H2PGm13BhpLLOf_yYgs";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
            + API_KEY;
    private final Gson gson = new Gson();

    public String generateShopAnalysis(String ordersData, String itemsData) {
        try {
            String prompt = "Analyse les ventes de la boutique à partir des données suivantes.\n\n" +
                    "Commandes :\n" + ordersData + "\n\n" +
                    "Articles vendus :\n" + itemsData + "\n\n" +
                    "Génère un résumé des statistiques (chiffre d'affaires total, produits les plus vendus et les moins vendus) et donne 3 recommandations business courtes et actionnables pour améliorer les ventes et la gestion des stocks. "
                    +
                    "Formate la réponse de manière lisible (texte clair, points de puces).";

            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);

            JsonArray parts = new JsonArray();
            parts.add(part);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject payload = new JsonObject();
            payload.add("contents", contents);

            String jsonBody = gson.toJson(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObj = firstCandidate.getAsJsonObject("content");
                    if (contentObj != null) {
                        JsonArray partsArray = contentObj.getAsJsonArray("parts");
                        if (partsArray != null && partsArray.size() > 0) {
                            return partsArray.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                }
            } else if (response.statusCode() == 429) {
                return "Erreur : Quota API atteint (HTTP 429). Vous avez dépassé la limite de requêtes gratuites pour cette clé Gemini. Veuillez vérifier votre facturation ou réessayer plus tard.";
            } else {
                return "Erreur lors de l'appel à l'API Gemini : HTTP " + response.statusCode() + "\n" + response.body();
            }
            return "Impossible d'extraire l'analyse.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion à l'IA : " + e.getMessage();
        }
    }
}
