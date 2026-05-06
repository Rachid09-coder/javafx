package com.edusmart.service;

import com.edusmart.model.PromoCode;

import java.util.Optional;

public interface PromoCodeService {
    boolean createPromoCode(PromoCode promoCode);
    Optional<PromoCode> getActivePromoByCode(String code);
    boolean hasStudentUsedPromo(int promoCodeId, int studentId);
    boolean markUsed(int promoCodeId, int studentId);
}

