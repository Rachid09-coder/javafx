package com.edusmart.dao;

import com.edusmart.model.Bulletin;

import java.util.List;
import java.util.Optional;

public interface BulletinDao {
    boolean create(Bulletin bulletin);
    List<Bulletin> findAll();
    Optional<Bulletin> findById(int id);
    boolean update(Bulletin bulletin);
    boolean delete(int id);
    int findRankByAverage(String semester, double average);
    void recalculateAllRanks(String academicYear, String semester);
}
