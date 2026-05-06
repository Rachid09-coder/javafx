package com.edusmart.dao;

import com.edusmart.model.Certification;

import java.util.List;
import java.util.Optional;

public interface CertificationDao {

    List<Certification> findAll();

    List<Certification> findByStudentId(int studentId);

    Optional<Certification> findById(int id);

    /**
     * Inserts a row; when supported, sets {@link Certification#setId(int)} from generated keys.
     */
    boolean create(Certification certification);

    /** Met à jour tous les champs d'une certification existante. */
    boolean update(Certification certification);

    /** Supprime une certification par son id. */
    boolean delete(int id);

    boolean updateRevocation(int id, String status, java.time.LocalDateTime revokedAt, String revocationReason);
}
