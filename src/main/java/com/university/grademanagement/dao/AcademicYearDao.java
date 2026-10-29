package com.university.grademanagement.dao;

import com.university.grademanagement.model.AcademicYear;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Academic Year management.
 */
public interface AcademicYearDao {
    Optional<AcademicYear> findById(int id);
    Optional<AcademicYear> getCurrentYear();
    List<AcademicYear> findAll();
    AcademicYear create(AcademicYear year);
    boolean setCurrentYear(int id);
}
