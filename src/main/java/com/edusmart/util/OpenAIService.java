package com.edusmart.util;

import com.edusmart.service.GeminiAiService;

public class OpenAIService {

    private final GeminiAiService geminiAiService = new GeminiAiService();

    public String generateShopAnalysis(String ordersSummary, String extraData) {
        String prompt = "Tu es un analyste business pour la boutique EduSmart. "
                + "Analyse les commandes et produits ci-dessous, puis propose des recommandations concretes en francais.\n\n"
                + "Commandes:\n" + (ordersSummary == null || ordersSummary.isBlank() ? "Aucune commande." : ordersSummary)
                + "\n\nDonnees additionnelles:\n" + (extraData == null || extraData.isBlank() ? "Aucune." : extraData);
        try {
            return geminiAiService.generateContent(prompt);
        } catch (Exception ex) {
            System.err.println("Gemini shop analysis failed. Using simulated response: " + ex.getMessage());
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Analyse boutique simulee\n\n");
        builder.append("Resume des commandes:\n").append(ordersSummary == null || ordersSummary.isBlank() ? "Aucune commande." : ordersSummary).append('\n');
        if (extraData != null && !extraData.isBlank()) {
            builder.append('\n').append("Donnees additionnelles:\n").append(extraData).append('\n');
        }
        builder.append('\n').append("Recommandations:\n");
        builder.append("- Mettre en avant les produits les plus demandes.\n");
        builder.append("- Reapprovisionner les produits a faible stock.\n");
        builder.append("- Ajouter des promotions ciblees pour augmenter le panier moyen.\n");
        return builder.toString();
    }
}
