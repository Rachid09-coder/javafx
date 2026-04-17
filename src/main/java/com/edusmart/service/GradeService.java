package com.edusmart.service;

import com.edusmart.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeService {
    boolean createGrade(Grade grade);
    List<Grade> getAllGrades();
    Optional<Grade> getGradeById(int id);
    List<Grade> getGradesByStudentId(int studentId);
    List<Grade> getGradesByStudentAndSemester(int studentId, String semester);
    boolean updateGrade(Grade grade);
    boolean deleteGrade(int id);
}
