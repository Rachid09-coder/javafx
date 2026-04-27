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
}

