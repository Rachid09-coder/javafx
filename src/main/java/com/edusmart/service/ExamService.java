package com.edusmart.service;

import com.edusmart.model.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamService {
    boolean createExam(Exam exam);
    List<Exam> getAllExams();
    Optional<Exam> getExamById(int id);
    boolean updateExam(Exam exam);
    boolean deleteExam(int id);
}
