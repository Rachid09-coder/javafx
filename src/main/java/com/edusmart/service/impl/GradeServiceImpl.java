package com.edusmart.service.impl;

import com.edusmart.dao.GradeDao;
import com.edusmart.model.Grade;
import com.edusmart.service.GradeService;

import java.util.List;
import java.util.Optional;

public class GradeServiceImpl implements GradeService {

    private final GradeDao gradeDao;

    public GradeServiceImpl(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

    @Override
    public boolean createGrade(Grade grade) {
        validateGrade(grade);
        return gradeDao.create(grade);
    }

    @Override
    public List<Grade> getAllGrades() {
        return gradeDao.findAll();
    }

    @Override
    public Optional<Grade> getGradeById(int id) {
        return gradeDao.findById(id);
    }

    @Override
    public List<Grade> getGradesByStudentId(int studentId) {
        return gradeDao.findByStudentId(studentId);
    }

    @Override
    public boolean updateGrade(Grade grade) {
        validateGrade(grade);
        return gradeDao.update(grade);
    }

    @Override
    public boolean deleteGrade(int id) {
        return gradeDao.delete(id);
    }

    /**
     * Logique métier : valide les données d'une note.
     */
    private void validateGrade(Grade grade) {
        if (grade.getSubject() == null || grade.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("La matière est obligatoire.");
        }
        if (grade.getScore() < 0) {
            throw new IllegalArgumentException("La note ne peut pas être négative.");
        }
        if (grade.getMaxScore() <= 0) {
            throw new IllegalArgumentException("La note maximale doit être supérieure à 0.");
        }
        if (grade.getScore() > grade.getMaxScore()) {
            throw new IllegalArgumentException("La note ne peut pas dépasser la note maximale.");
        }
        if (grade.getStudentId() <= 0) {
            throw new IllegalArgumentException("L'étudiant est obligatoire.");
        }
        if (grade.getSemester() == null || grade.getSemester().trim().isEmpty()) {
            throw new IllegalArgumentException("Le semestre est obligatoire.");
        }
    }
}
