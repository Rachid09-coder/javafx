package com.edusmart.service.impl;

import com.edusmart.model.PlagiarismResult;
import com.edusmart.service.PlagiarismDetectionService;
import java.util.List;

public class PlagiarismDetectionServiceImpl implements PlagiarismDetectionService {

    @Override
    public PlagiarismResult checkPlagiarism(String text, List<String> compareTo) {
        // Mock implementation using Levenshtein-like distance or simple overlap.
        double maxSimilarity = 0.0;
        String summary = "Aucun plagiat significatif détecté.";

        if (text == null || text.isEmpty()) return new PlagiarismResult(0.0, "Texte vide.");

        for (String source : compareTo) {
            double similarity = calculateSimilarity(text, source);
            if (similarity > maxSimilarity) maxSimilarity = similarity;
        }

        if (maxSimilarity > 0.3) {
            summary = String.format("Alerte : %.1f%% de similarité détectée avec d'autres copies.", maxSimilarity * 100);
        }

        return new PlagiarismResult(maxSimilarity, summary);
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        
        // Normalize and tokenize
        java.util.Set<String> set1 = tokenize(s1);
        java.util.Set<String> set2 = tokenize(s2);
        
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        
        // Intersection
        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        
        // Union
        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        
        // Jaccard similarity index
        return (double) intersection.size() / union.size();
    }

    private java.util.Set<String> tokenize(String text) {
        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", "")
                .split("\\s+");
        return new java.util.HashSet<>(java.util.Arrays.asList(words));
    }
}
