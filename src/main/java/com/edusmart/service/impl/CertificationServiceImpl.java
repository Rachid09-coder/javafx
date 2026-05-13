package com.edusmart.service.impl;

import com.edusmart.dao.CertificationDao;
import com.edusmart.model.Certification;
import com.edusmart.service.CertificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CertificationServiceImpl implements CertificationService {

    private final CertificationDao certificationDao;

    public CertificationServiceImpl(CertificationDao certificationDao) {
        this.certificationDao = certificationDao;
    }

    @Override
    public List<Certification> getAllCertifications() {
        return certificationDao.findAll();
    }

    @Override
    public List<Certification> getCertificationsForStudent(int studentId) {
        return certificationDao.findByStudentId(studentId);
    }

    @Override
    public Optional<Certification> getCertificationById(int id) {
        return certificationDao.findById(id);
    }

    @Override
    public boolean issueCertification(Certification certification) {
        validateCertification(certification);
        if (certification.getVerificationCode() == null || certification.getVerificationCode().isBlank()) {
            certification.setVerificationCode(newVerificationCode());
        }
        if (certification.getUniqueNumber() == null || certification.getUniqueNumber().isBlank()) {
            certification.setUniqueNumber(newUniqueNumber());
        }
        if (certification.getStatus() == null || certification.getStatus().isBlank()) {
            certification.setStatus(Certification.STATUS_ISSUED);
        }
        if (certification.getIssuedAt() == null) {
            certification.setIssuedAt(LocalDateTime.now());
        }
        return certificationDao.create(certification);
    }

    @Override
    public boolean updateCertification(Certification certification) {
        validateCertification(certification);
        return certificationDao.update(certification);
    }

    @Override
    public boolean deleteCertification(int id) {
        return certificationDao.delete(id);
    }

    @Override
    public boolean revokeCertification(int id, String revocationReason) {
        if (revocationReason == null || revocationReason.trim().isEmpty()) {
            throw new IllegalArgumentException("La raison de révocation est obligatoire.");
        }
        return certificationDao.updateRevocation(
                id,
                Certification.STATUS_REVOKED,
                LocalDateTime.now(),
                revocationReason
        );
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateCertification(Certification c) {
        if (c.getCertificationType() == null || c.getCertificationType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de certification est obligatoire.");
        }
        if (c.getStudentId() <= 0) {
            throw new IllegalArgumentException("L'identifiant de l'étudiant est obligatoire.");
        }
        if (c.getStatus() == null || c.getStatus().trim().isEmpty()) {
            c.setStatus(Certification.STATUS_ISSUED);
        }
    }

    private static String newVerificationCode() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return raw.length() <= 30 ? raw : raw.substring(0, 30);
    }

    private static String newUniqueNumber() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return raw.length() <= 50 ? raw : raw.substring(0, 50);
    }
}
