package com.edusmart.service.impl;

import com.edusmart.dao.BulletinDao;
import com.edusmart.dao.CourseDao;
import com.edusmart.dao.GradeDao;
import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcGradeDao;
import com.edusmart.model.Bulletin;
import com.edusmart.model.Course;
import com.edusmart.model.Grade;
import com.edusmart.service.BulletinService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BulletinServiceImpl implements BulletinService {

    private final BulletinDao bulletinDao;
    private final GradeDao gradeDao;
    private final CourseDao courseDao;

    public BulletinServiceImpl(BulletinDao bulletinDao) {
        this.bulletinDao = bulletinDao;
        this.gradeDao = new JdbcGradeDao();
        this.courseDao = new JdbcCourseDao();
    }

    public BulletinServiceImpl(BulletinDao bulletinDao, GradeDao gradeDao, CourseDao courseDao) {
        this.bulletinDao = bulletinDao;
        this.gradeDao = gradeDao;
        this.courseDao = courseDao;
    }

    @Override
    public boolean createBulletin(Bulletin bulletin) {
        validateBulletin(bulletin);
        applyMention(bulletin);
        if (bulletin.getCreatedAt() == null) {
            bulletin.setCreatedAt(LocalDateTime.now());
        }
        bulletin.setUpdatedAt(LocalDateTime.now());
        if (bulletin.getStatus() == null || bulletin.getStatus().isBlank()) {
            bulletin.setStatus("DRAFT");
        }
        return bulletinDao.create(bulletin);
    }

    @Override
    public List<Bulletin> getAllBulletins() {
        return bulletinDao.findAll();
    }

    @Override
    public Optional<Bulletin> getBulletinById(int id) {
        return bulletinDao.findById(id);
    }

    @Override
    public boolean updateBulletin(Bulletin bulletin) {
        validateBulletin(bulletin);
        applyMention(bulletin);
        bulletin.setUpdatedAt(LocalDateTime.now());
        return bulletinDao.update(bulletin);
    }

    @Override
    public boolean deleteBulletin(int id) {
        return bulletinDao.delete(id);
    }

    // ── Logique métier : calcul de la mention ─────────────────────────────
    /**
     * Calcule et affecte automatiquement la mention selon la moyenne.
     * Barème : <10 → "Ajourné", 10-12 → "Passable", 12-14 → "Assez Bien",
     *          14-16 → "Bien", 16-18 → "Très Bien", ≥18 → "Excellent"
     */
    private void applyMention(Bulletin bulletin) {
        Double avg = bulletin.getAverage();
        if (avg == null) return;
        String mention;
        if (avg < 10) {
            mention = "Ajourné";
        } else if (avg < 12) {
            mention = "Passable";
        } else if (avg < 14) {
            mention = "Assez Bien";
        } else if (avg < 16) {
            mention = "Bien";
        } else if (avg < 18) {
            mention = "Très Bien";
        } else {
            mention = "Excellent";
        }
        // Only override if not manually set
        if (bulletin.getMention() == null || bulletin.getMention().isBlank()) {
            bulletin.setMention(mention);
        }
    }

    // ── Validation des champs ─────────────────────────────────────────────
    private void validateBulletin(Bulletin bulletin) {
        if (bulletin.getAcademicYear() == null || bulletin.getAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("L'année académique est obligatoire.");
        }
        if (bulletin.getSemester() == null || bulletin.getSemester().trim().isEmpty()) {
            throw new IllegalArgumentException("Le semestre est obligatoire.");
        }
        if (bulletin.getStudentId() <= 0) {
            throw new IllegalArgumentException("L'étudiant est obligatoire.");
        }
        Double avg = bulletin.getAverage();
        if (bulletin.getAverage() != null && (avg < 0 || avg > 20)) {
            throw new IllegalArgumentException("La moyenne doit être comprise entre 0 et 20.");
        }
    }

    @Override
    public Double calculateStudentAverage(int studentId, String semester) {
        List<Grade> grades = gradeDao.findByStudentId(studentId).stream()
                .filter(g -> semester == null || semester.equalsIgnoreCase(g.getSemester()))
                .toList();
        
        if (grades.isEmpty()) return null;

        double totalWeightedScore = 0;
        double totalCoefficients = 0;

        for (Grade g : grades) {
            double coeff = 1.0;
            Optional<Course> course = courseDao.findById(g.getCourseId());
            if (course.isPresent() && course.get().getCoefficient() != null) {
                coeff = course.get().getCoefficient();
            }
            totalWeightedScore += g.getNote() * coeff;
            totalCoefficients += coeff;
        }

        if (totalCoefficients == 0) return 0.0;
        return totalWeightedScore / totalCoefficients;
    }

    @Override
    public Integer calculateStudentRank(int studentId, String semester, Double average) {
        if (average == null) return null;
        return bulletinDao.findRankByAverage(semester, average);
    }
}
