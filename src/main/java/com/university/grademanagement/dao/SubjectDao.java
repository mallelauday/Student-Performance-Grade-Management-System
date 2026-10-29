package com.university.grademanagement.dao;

import com.university.grademanagement.model.Subject;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Subject/Course management.
 */
public interface SubjectDao {
    Optional<Subject> findById(int id);
    Optional<Subject> findByCode(String code);
    Subject create(Subject subject);
    boolean update(Subject subject);
    boolean setActive(int id, boolean active);
    List<Subject> findAll(String department, Integer semester, Boolean activeOnly);
    List<Subject> findByDepartmentAndSemester(String department, int semester);
    int count();
}
