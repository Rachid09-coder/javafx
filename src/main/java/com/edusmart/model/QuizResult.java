package com.edusmart.model;

import java.util.List;

public class QuizResult {
    private String subject;
    private int numQuestions;
    private String difficulty;
    private String level;
    private List<ExamQuestion> questions;

    public QuizResult(String subject, int numQuestions, String difficulty, String level, List<ExamQuestion> questions) {
        this.subject = subject;
        this.numQuestions = numQuestions;
        this.difficulty = difficulty;
        this.level = level;
        this.questions = questions;
    }

    public String getSubject() { return subject; }
    public int getNumQuestions() { return numQuestions; }
    public String getDifficulty() { return difficulty; }
    public String getLevel() { return level; }
    public List<ExamQuestion> getQuestions() { return questions; }
}
