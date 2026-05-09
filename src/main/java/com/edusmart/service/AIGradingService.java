package com.edusmart.service;

import com.edusmart.model.AIGradingResult;
import com.edusmart.model.ExamQuestion;
import com.edusmart.model.ExamSubmission;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Async AI grading service. All methods are non-blocking and return CompletableFutures
 * so the JavaFX UI thread is never blocked.
 */
public interface AIGradingService {

    /**
     * Grades a single student answer asynchronously using OpenAI.
     *
     * @param question   The exam question with correct answer and max points.
     * @param submission The student's submission to grade.
     * @return A future resolving to an {@link AIGradingResult}.
     */
    CompletableFuture<AIGradingResult> gradeAnswerAsync(ExamQuestion question, ExamSubmission submission);

    /**
     * Grades all student submissions for a single question asynchronously.
     *
     * @param question    The exam question.
     * @param submissions List of student submissions to grade.
     * @return A future resolving to a list of grading results (same order as submissions).
     */
    CompletableFuture<List<AIGradingResult>> gradeAllAnswersAsync(ExamQuestion question,
                                                                   List<ExamSubmission> submissions);
}
