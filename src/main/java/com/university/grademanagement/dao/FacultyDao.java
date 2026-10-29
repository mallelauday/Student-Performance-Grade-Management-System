package com.university.grademanagement.dao;

import com.university.grademanagement.model.Faculty;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Faculty entity management.
 */
public interface FacultyDao {
    Optional<Faculty> findById(int id);
    Optional<Faculty> findByUserId(int userId);
    Optional<Faculty> findByEmployeeId(String employeeId);
    Faculty create(Faculty faculty, Connection conn);
    boolean update(Faculty faculty);
    boolean setActive(int id, boolean active);
    List<Faculty> findAll(boolean activeOnly);
    int count();
}
