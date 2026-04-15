package com.edusmart.service.impl;

import com.edusmart.dao.BulletinDao;
import com.edusmart.model.Bulletin;
import com.edusmart.service.BulletinService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BulletinServiceImpl implements BulletinService {

    private final BulletinDao bulletinDao;

    public BulletinServiceImpl(BulletinDao bulletinDao) {
        this.bulletinDao = bulletinDao;
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
        if (avg != null && (avg < 0 || avg > 20)) {
            throw new IllegalArgumentException("La moyenne doit être comprise entre 0 et 20.");
        }
    }
}
