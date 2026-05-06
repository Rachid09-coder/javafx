package com.edusmart.service;

import com.edusmart.model.Module;

import java.util.List;
import java.util.Optional;

public interface ModuleService {
    boolean createModule(Module module);
    List<Module> getAllModules();
    Optional<Module> getModuleById(int id);
    boolean updateModule(Module module);
    boolean deleteModule(int id);
}
