package com.edusmart.dao;

import com.edusmart.model.ExamSubmission;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for ExamSubmission CRUD operations.
 */
public interface ExamSubmissionDao {
    boolean create(ExamSubmission submission);
    List<ExamSubmission> findByExamId(int examId);
    List<com.edusmart.model.StudentSubmission> getStudentSubmissions();
    List<com.edusmart.model.StudentSubmission> getStudentSubmissions(int examId);
    List<ExamSubmission> findByStudentId(int studentId);
    List<ExamSubmission> findByExamAndStudent(int examId, int studentId);
    List<ExamSubmission> findByQuestionId(int questionId);
    Optional<ExamSubmission> findById(int id);
    boolean update(ExamSubmission submission);
    boolean delete(int id);
}
