package com.edusmart.service;

import com.edusmart.model.Bulletin;

import java.util.List;
import java.util.Optional;

public interface BulletinService {
    boolean createBulletin(Bulletin bulletin);
    List<Bulletin> getAllBulletins();
    Optional<Bulletin> getBulletinById(int id);
    boolean updateBulletin(Bulletin bulletin);
    boolean deleteBulletin(int id);
}
