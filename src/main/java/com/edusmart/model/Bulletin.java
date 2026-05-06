package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Bulletin model — maps to the {@code bulletin} table.
 */
public class Bulletin {

    private int id;
    private String academicYear;
    private String semester;
    private Double average;
    private String status;
    private String mention;
    private Integer classRank;
    private String hmacHash;
    private String pdfPath;
    private String verificationCode;
    private LocalDateTime validatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int studentId;
    private Integer validatedById;
    private Integer publishedById;

    public Bulletin() {}

    // Getters & setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public Integer getClassRank() {
        return classRank;
    }

    public void setClassRank(Integer classRank) {
        this.classRank = classRank;
    }

    public String getHmacHash() {
        return hmacHash;
    }

    public void setHmacHash(String hmacHash) {
        this.hmacHash = hmacHash;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Integer getValidatedById() {
        return validatedById;
    }

    public void setValidatedById(Integer validatedById) {
        this.validatedById = validatedById;
    }

    public Integer getPublishedById() {
        return publishedById;
    }

    public void setPublishedById(Integer publishedById) {
        this.publishedById = publishedById;
    }
}
