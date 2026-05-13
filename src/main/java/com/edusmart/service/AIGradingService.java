package com.edusmart.service;

import com.edusmart.model.AIGradingResult;
import com.edusmart.model.ExamQuestion;

public interface AIGradingService {
    /**
     * Grades a student answer based on the question and correct answer.
     */
    AIGradingResult gradeAnswer(ExamQuestion question, String studentAnswer);
}
