package com.edusmart.dao;

import com.edusmart.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeDao {
    boolean create(Grade grade);
    List<Grade> findAll();
    Optional<Grade> findById(int id);
    List<Grade> findByStudentId(int studentId);
    boolean update(Grade grade);
    boolean delete(int id);
}
