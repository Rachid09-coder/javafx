package com.edusmart.dao;

import com.edusmart.model.Module;

import java.util.List;
import java.util.Optional;

public interface ModuleDao {
    boolean create(Module module);
    List<Module> findAll();
    Optional<Module> findById(int id);
    boolean update(Module module);
    boolean delete(int id);
}
