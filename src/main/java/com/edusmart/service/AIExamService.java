package com.edusmart.service;

import com.edusmart.model.ExamQuestion;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AIExamService - Handles automatic generation of exam questions.
 */
public interface AIExamService {
    /**
     * Generates a list of QCM questions based on a subject.
     * @param subject The topic (e.g., Maths, Informatique)
     * @param count Number of questions to generate
     * @return Future containing the generated questions
     */
    CompletableFuture<List<ExamQuestion>> generateQCM(String subject, int count);
}
