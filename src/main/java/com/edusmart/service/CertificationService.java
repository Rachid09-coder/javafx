package com.edusmart.service;

import com.edusmart.model.Certification;

import java.util.List;
import java.util.Optional;

public interface CertificationService {

    List<Certification> getAllCertifications();

    List<Certification> getCertificationsForStudent(int studentId);

    Optional<Certification> getCertificationById(int id);

    boolean issueCertification(Certification certification);

    boolean revokeCertification(int id, String revocationReason);
}
