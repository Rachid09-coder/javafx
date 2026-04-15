package com.edusmart.service.impl;

import com.edusmart.dao.ModuleDao;
import com.edusmart.model.Module;
import com.edusmart.service.ModuleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ModuleServiceImpl implements ModuleService {

    private final ModuleDao moduleDao;

    public ModuleServiceImpl(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @Override
    public boolean createModule(Module module) {
        validateModule(module);
        if (module.getCreatedAt() == null) {
            module.setCreatedAt(LocalDateTime.now());
        }
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
        validateModule(module);
        return moduleDao.update(module);
    }

    @Override
    public boolean deleteModule(int id) {
        return moduleDao.delete(id);
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateModule(Module module) {
        if (module.getTitle() == null || module.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du module est obligatoire.");
        }
        if (module.getTitle().trim().length() < 2) {
            throw new IllegalArgumentException("Le titre du module doit contenir au moins 2 caractères.");
        }
        if (module.getDurationHours() < 0) {
            throw new IllegalArgumentException("La durée ne peut pas être négative.");
        }
    }
}
