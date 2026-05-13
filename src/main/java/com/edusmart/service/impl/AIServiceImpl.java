package com.edusmart.service.impl;

import com.edusmart.service.AIService;
import com.edusmart.service.GeminiAiService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIServiceImpl implements AIService {

    private final GeminiAiService geminiAiService;
    private final ExecutorService executor;

    public AIServiceImpl() {
        this.geminiAiService = new GeminiAiService();
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<String> askAI(String userMessage, String courseContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = "Tu es un assistant pedagogique expert d'EduSmart. "
                        + "Aide l'etudiant a comprendre le cours avec des explications claires et concises.\n\n"
                        + "Contexte du cours actuel:\n" + courseContext + "\n\n"
                        + "Question de l'utilisateur:\n" + userMessage;
                return geminiAiService.generateContent(prompt).trim();
            } catch (Exception e) {
                e.printStackTrace();
                return "Une erreur s'est produite lors de la connexion a l'assistant IA: " + e.getMessage();
            }
        }, executor);
    }
}
