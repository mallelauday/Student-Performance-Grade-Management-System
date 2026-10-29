package com.university.grademanagement.dao;

import com.university.grademanagement.model.Student;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Student entity management.
 */
public interface StudentDao {
    Optional<Student> findById(int id);
    Optional<Student> findByUserId(int userId);
    Optional<Student> findByRollNumber(String rollNumber);
    Student create(Student student, Connection conn);
    boolean update(Student student);
    boolean setActive(int id, boolean active);
    List<Student> findAll(String searchKeyword, String department, Integer semester, Boolean activeOnly, int offset, int limit);
    int count(String searchKeyword, String department, Integer semester, Boolean activeOnly);
    List<String> getDistinctDepartments();
    boolean existsByRollNumber(String rollNumber, Integer excludeId);
}
