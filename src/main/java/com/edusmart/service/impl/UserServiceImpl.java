package com.edusmart.service.impl;

import com.edusmart.dao.UserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {

    /** Regex email RFC-5322 simplifié */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");

    /** Regex numéro de téléphone : 8 chiffres minimum */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[\\d\\s\\-]{8,15}$");

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean createUser(User user) {
        validateUser(user, true);
        return userDao.create(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public Optional<User> getUserById(int id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByEmailAssoc(String emailAssoc) {
        return userDao.findByEmailAssoc(emailAssoc);
    }

    @Override
    public boolean updateUser(User user) {
        validateUser(user, false);
        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return userDao.delete(id);
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateUser(User user, boolean requirePassword) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Format d'email invalide (ex: nom@domaine.com).");
        }
        if (requirePassword) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Le mot de passe est obligatoire.");
            }
            if (user.getPassword().length() < 6) {
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
            }
        }
        if (user.getNumtel() != null && !user.getNumtel().isBlank()) {
            if (!PHONE_PATTERN.matcher(user.getNumtel().trim()).matches()) {
                throw new IllegalArgumentException("Format de téléphone invalide (ex: 0612345678).");
            }
        }
        if (user.getRole() == null && (user.getRoleValue() == null || user.getRoleValue().isBlank())) {
            throw new IllegalArgumentException("Le rôle de l'utilisateur est obligatoire.");
        }
    }
}
