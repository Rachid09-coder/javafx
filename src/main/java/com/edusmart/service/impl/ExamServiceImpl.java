package com.edusmart.service.impl;

import com.edusmart.dao.ExamDao;
import com.edusmart.model.Exam;
import com.edusmart.service.ExamService;

import java.util.List;
import java.util.Optional;

public class ExamServiceImpl implements ExamService {

    private final ExamDao examDao;

    public ExamServiceImpl(ExamDao examDao) {
        this.examDao = examDao;
    }

    @Override
    public boolean createExam(Exam exam) {
        validateExam(exam);
        return examDao.create(exam);
    }

    @Override
    public List<Exam> getAllExams() {
        return examDao.findAll();
    }

    @Override
    public Optional<Exam> getExamById(int id) {
        return examDao.findById(id);
    }

    @Override
    public boolean updateExam(Exam exam) {
        validateExam(exam);
        return examDao.update(exam);
    }

    @Override
    public boolean deleteExam(int id) {
        return examDao.delete(id);
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateExam(Exam exam) {
        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'examen est obligatoire.");
        }
        if (exam.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("Le titre doit contenir au moins 3 caractères.");
        }
        Integer duration = exam.getDuration();
        if (duration != null && duration <= 0) {
            throw new IllegalArgumentException("La durée doit être supérieure à 0.");
        }
        Double coeff = exam.getCoefficient();
        if (coeff != null && coeff <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");
        }
        String type = exam.getType();
        if (type != null && !type.isBlank()) {
            // type is free-text, just ensure it's not too long
            if (type.length() > 100) {
                throw new IllegalArgumentException("Le type de l'examen ne peut pas dépasser 100 caractères.");
            }
        }
    }
}
