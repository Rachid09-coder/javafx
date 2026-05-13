package com.edusmart.util;

public class OpenAIService {

    public String generateShopAnalysis(String ordersSummary, String extraData) {
        StringBuilder builder = new StringBuilder();
        builder.append("Analyse boutique simulée\n\n");
        builder.append("Résumé des commandes:\n").append(ordersSummary == null || ordersSummary.isBlank() ? "Aucune commande." : ordersSummary).append('\n');
        if (extraData != null && !extraData.isBlank()) {
            builder.append('\n').append("Données additionnelles:\n").append(extraData).append('\n');
        }
        builder.append('\n').append("Recommandations:\n");
        builder.append("- Mettre en avant les produits les plus demandés.\n");
        builder.append("- Réapprovisionner les produits à faible stock.\n");
        builder.append("- Ajouter des promotions ciblées pour augmenter le panier moyen.\n");
        return builder.toString();
    }
}