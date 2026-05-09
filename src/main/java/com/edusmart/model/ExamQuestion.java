package com.edusmart.model;

/**
 * Represents a single question inside an exam.
 * Supports MCQ, open-ended, true/false, and short-answer types.
 */
public class ExamQuestion {

    public enum QuestionType {
        MCQ, OPEN_ENDED, TRUE_FALSE, SHORT_ANSWER
    }

    private int id;
    private int examId;
    private String questionText;
    private QuestionType questionType;
    private String correctAnswer;
    private double maxPoints;
    private int orderIndex;
    /** Pipe-separated list of choices for MCQ questions. */
    private String options;

    public ExamQuestion() {}

    public ExamQuestion(int examId, String questionText, QuestionType questionType, double maxPoints) {
        this.examId = examId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.maxPoints = maxPoints;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public double getMaxPoints() { return maxPoints; }
    public void setMaxPoints(double maxPoints) { this.maxPoints = maxPoints; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    @Override
    public String toString() {
        String preview = questionText != null && questionText.length() > 55
                ? questionText.substring(0, 55) + "\u2026" : questionText;
        return "Q" + (orderIndex + 1) + ": " + preview;
    }
}
