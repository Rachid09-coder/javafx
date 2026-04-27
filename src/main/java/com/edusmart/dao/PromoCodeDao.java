package com.edusmart.dao;

import com.edusmart.model.PromoCode;

import java.util.Optional;

public interface PromoCodeDao {
    Optional<PromoCode> findActiveByCode(String code);
    boolean hasStudentUsedPromo(int promoCodeId, int studentId);
    boolean markUsed(int promoCodeId, int studentId);
}

