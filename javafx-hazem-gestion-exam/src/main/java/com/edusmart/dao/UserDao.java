package com.edusmart.dao;

import com.edusmart.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    boolean create(User user);
    List<User> findAll();
    Optional<User> findById(int id);
    Optional<User> findByEmail(String email);
    boolean update(User user);
    boolean delete(int id);
}
