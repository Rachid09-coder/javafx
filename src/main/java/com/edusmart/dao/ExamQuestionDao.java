package com.edusmart.dao;

import com.edusmart.model.ExamQuestion;
import java.util.List;

public interface ExamQuestionDao {
    List<ExamQuestion> findByExamId(int examId);
    ExamQuestion findById(int id);
    boolean create(ExamQuestion question);
    boolean update(ExamQuestion question);
    boolean delete(int id);
}
