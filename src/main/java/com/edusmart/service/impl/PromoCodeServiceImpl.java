package com.edusmart.service.impl;

import com.edusmart.dao.PromoCodeDao;
import com.edusmart.model.PromoCode;
import com.edusmart.service.PromoCodeService;

import java.util.Optional;

public class PromoCodeServiceImpl implements PromoCodeService {
    private final PromoCodeDao promoCodeDao;

    public PromoCodeServiceImpl(PromoCodeDao promoCodeDao) {
        this.promoCodeDao = promoCodeDao;
    }

    @Override
    public boolean createPromoCode(PromoCode promoCode) {
        validate(promoCode);
        return promoCodeDao.create(promoCode);
    }

    @Override
    public Optional<PromoCode> getActivePromoByCode(String code) {
        return promoCodeDao.findActiveByCode(code);
    }

    @Override
    public boolean hasStudentUsedPromo(int promoCodeId, int studentId) {
        return promoCodeDao.hasStudentUsedPromo(promoCodeId, studentId);
    }

    @Override
    public boolean markUsed(int promoCodeId, int studentId) {
        return promoCodeDao.markUsed(promoCodeId, studentId);
    }

    private void validate(PromoCode promoCode) {
        if (promoCode == null) throw new IllegalArgumentException("Code promo requis.");
        if (promoCode.getCode() == null || promoCode.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire.");
        }
        String code = promoCode.getCode().trim();
        if (code.length() < 2) throw new IllegalArgumentException("Le code doit contenir au moins 2 caractères.");
        if (code.length() > 50) throw new IllegalArgumentException("Le code ne peut pas dépasser 50 caractères.");
        double pct = promoCode.getDiscountPercent();
        if (pct < 0 || pct > 100) throw new IllegalArgumentException("La réduction doit être entre 0 et 100.");
    }
}

