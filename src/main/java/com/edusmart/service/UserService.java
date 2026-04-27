package com.edusmart.service;

import com.edusmart.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    boolean createUser(User user);
    List<User> getAllUsers();
    Optional<User> getUserById(int id);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByEmailAssoc(String emailAssoc);
    Optional<User> getUserByGoogleId(String googleId);
    boolean updateUser(User user);
    boolean deleteUser(int id);
}
