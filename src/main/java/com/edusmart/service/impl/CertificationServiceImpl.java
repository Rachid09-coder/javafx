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
        if (certification.getVerificationCode() == null || certification.getVerificationCode().isBlank()) {
            certification.setVerificationCode(newVerificationCode());
        }
        if (certification.getUniqueNumber() == null || certification.getUniqueNumber().isBlank()) {
            certification.setUniqueNumber(newUniqueNumber());
        }
        if (certification.getStatus() == null || certification.getStatus().isBlank()) {
            certification.setStatus(Certification.STATUS_ISSUED);
        }
        return certificationDao.create(certification);
    }

    @Override
    public boolean revokeCertification(int id, String revocationReason) {
        return certificationDao.updateRevocation(
                id,
                Certification.STATUS_REVOKED,
                LocalDateTime.now(),
                revocationReason != null ? revocationReason : ""
        );
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
