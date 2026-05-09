package com.edusmart.service.impl;

import com.edusmart.dao.UserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean createUser(User user) {
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
    public boolean updateUser(User user) {
        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return userDao.delete(id);
    }
}
