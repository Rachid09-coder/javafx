package com.edusmart.service.impl;

import com.edusmart.dao.BulletinDao;
import com.edusmart.model.Bulletin;
import com.edusmart.service.BulletinService;

import java.util.List;
import java.util.Optional;

public class BulletinServiceImpl implements BulletinService {

    private final BulletinDao bulletinDao;

    public BulletinServiceImpl(BulletinDao bulletinDao) {
        this.bulletinDao = bulletinDao;
    }

    @Override
    public boolean createBulletin(Bulletin bulletin) {
        return bulletinDao.create(bulletin);
    }

    @Override
    public List<Bulletin> getAllBulletins() {
        return bulletinDao.findAll();
    }

    @Override
    public Optional<Bulletin> getBulletinById(int id) {
        return bulletinDao.findById(id);
    }

    @Override
    public boolean updateBulletin(Bulletin bulletin) {
        return bulletinDao.update(bulletin);
    }

    @Override
    public boolean deleteBulletin(int id) {
        return bulletinDao.delete(id);
    }
}
