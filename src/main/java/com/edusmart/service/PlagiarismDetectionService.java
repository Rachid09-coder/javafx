package com.edusmart.service;

import com.edusmart.model.ExamSubmission;
import com.edusmart.model.PlagiarismResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Plagiarism detection service using TF-IDF cosine similarity.
 * No external API needed – pure Java implementation.
 */
public interface PlagiarismDetectionService {

    /**
     * Runs plagiarism detection across all submissions for a single question.
     * Every submission is compared against all others.
     *
     * @param submissions List of student answer submissions for one question.
     * @return A future resolving to plagiarism results (one per submission).
     */
    CompletableFuture<List<PlagiarismResult>> detectPlagiarismAsync(List<ExamSubmission> submissions);

    /**
     * Calculates the cosine similarity between two text strings.
     *
     * @param text1 First text.
     * @param text2 Second text.
     * @return Similarity in range [0.0, 100.0].
     */
    double calculateSimilarity(String text1, String text2);
    /**
     * Extracts text from two PDFs and compares them.
     *
     * @param f1 First PDF file.
     * @param f2 Second PDF file.
     * @return Similarity score (0-100).
     */
    double comparePDF(java.io.File f1, java.io.File f2);
}
