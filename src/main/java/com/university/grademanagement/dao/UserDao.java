package com.university.grademanagement.dao;

import com.university.grademanagement.model.User;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user account operations.
 */
public interface UserDao {
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User create(User user, Connection conn);
    boolean update(User user);
    boolean updatePassword(int userId, String newHash);
    boolean updateLastLogin(int userId);
    boolean setActive(int userId, boolean active);
    List<User> findAll();
    int count();
}
