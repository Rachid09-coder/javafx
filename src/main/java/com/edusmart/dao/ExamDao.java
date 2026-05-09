package com.edusmart.dao;

import com.edusmart.model.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamDao {
    boolean create(Exam exam);
    List<Exam> findAll();
    Optional<Exam> findById(int id);
    boolean update(Exam exam);
    boolean delete(int id);
}
