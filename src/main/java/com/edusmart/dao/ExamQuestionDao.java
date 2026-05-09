package com.edusmart.dao;

import com.edusmart.model.ExamQuestion;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for ExamQuestion CRUD operations.
 */
public interface ExamQuestionDao {
    boolean create(ExamQuestion question);
    List<ExamQuestion> findByExamId(int examId);
    Optional<ExamQuestion> findById(int id);
    boolean update(ExamQuestion question);
    boolean delete(int id);
    boolean deleteByExamId(int examId);
}
