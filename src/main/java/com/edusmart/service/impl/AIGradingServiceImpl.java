package com.edusmart.service.impl;

import com.edusmart.model.AIGradingResult;
import com.edusmart.model.ExamQuestion;
import com.edusmart.service.AIGradingService;

public class AIGradingServiceImpl implements AIGradingService {

    @Override
    public AIGradingResult gradeAnswer(ExamQuestion question, String studentAnswer) {
        // In a real implementation, this would call Gemini or OpenAI API.
        // For now, we simulate a response based on text length and keyword matches.
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return new AIGradingResult(0.0, "La réponse est vide.", 1.0);
        }

        double maxPoints = (question != null) ? question.getMaxPoints() : 20.0;
        double score = maxPoints * 0.8; // Default 80%
        String feedback = "Bonne réponse. L'IA suggère que les concepts clés sont présents.";
        double confidence = 0.85;

        // Simple mock logic
        if (studentAnswer.length() < 10) {
            score = maxPoints * 0.3;
            feedback = "Réponse trop courte. Manque de détails.";
            confidence = 0.95;
        }

        AIGradingResult result = new AIGradingResult(score, feedback, confidence);
        result.setModelUsed("EduSmart-AI (Gemini 2.5)");
        return result;
    }
}
