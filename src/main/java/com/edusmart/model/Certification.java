package com.edusmart.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Maps to the {@code certification} table.
 */
public class Certification {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REVOKED = "REVOKED";

    private int id;
    /** DB column {@code type} — kind of certification (e.g. diploma label). */
    private String certificationType;
    private LocalDateTime issuedAt;
    private String verificationCode;
    private String pdfPath;
    private String status;
    private String uniqueNumber;
    private LocalDateTime validUntil;
    private String hmacHash;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private int studentId;
    private Integer bulletinId;

    public Certification() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(String certificationType) {
        this.certificationType = certificationType;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUniqueNumber() {
        return uniqueNumber;
    }

    public void setUniqueNumber(String uniqueNumber) {
        this.uniqueNumber = uniqueNumber;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getHmacHash() {
        return hmacHash;
    }

    public void setHmacHash(String hmacHash) {
        this.hmacHash = hmacHash;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Integer getBulletinId() {
        return bulletinId;
    }

    public void setBulletinId(Integer bulletinId) {
        this.bulletinId = bulletinId;
    }

    public boolean isIssued() {
        return STATUS_ISSUED.equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Certification{id=" + id + ", type='" + certificationType + "', studentId=" + studentId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Certification that = (Certification) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
