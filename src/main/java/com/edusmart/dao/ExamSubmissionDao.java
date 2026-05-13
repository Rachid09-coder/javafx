package com.edusmart.dao;

import com.edusmart.model.ExamSubmission;
import java.util.List;

public interface ExamSubmissionDao {
    List<ExamSubmission> findByExamAndQuestion(int examId, int questionId);
    List<ExamSubmission> findByExam(int examId);
    ExamSubmission findById(int id);
    ExamSubmission findByStudentAndExam(int studentId, int examId);
    boolean update(ExamSubmission submission);
    boolean create(ExamSubmission submission);
}
