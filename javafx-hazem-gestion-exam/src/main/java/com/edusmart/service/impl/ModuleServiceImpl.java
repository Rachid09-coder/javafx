package com.edusmart.service.impl;

import com.edusmart.dao.ModuleDao;
import com.edusmart.model.Module;
import com.edusmart.service.ModuleService;

import java.util.List;
import java.util.Optional;

public class ModuleServiceImpl implements ModuleService {

    private final ModuleDao moduleDao;

    public ModuleServiceImpl(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @Override
    public boolean createModule(Module module) {
        return moduleDao.create(module);
    }

    @Override
    public List<Module> getAllModules() {
        return moduleDao.findAll();
    }

    @Override
    public Optional<Module> getModuleById(int id) {
        return moduleDao.findById(id);
    }

    @Override
    public boolean updateModule(Module module) {
        return moduleDao.update(module);
    }

    @Override
    public boolean deleteModule(int id) {
        return moduleDao.delete(id);
    }
}
