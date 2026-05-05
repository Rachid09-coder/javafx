package com.edusmart.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for interacting with the OpenAI API.
 */
public interface AIService {
    
    /**
     * Sends a prompt to the AI assistant along with the course context.
     * @param userMessage The question or prompt from the user.
     * @param courseContext The background context (e.g. course title and description).
     * @return A CompletableFuture that will complete with the AI's response text.
     */
    CompletableFuture<String> askAI(String userMessage, String courseContext);
}
