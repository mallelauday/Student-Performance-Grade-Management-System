package com.university.grademanagement.dao;

import com.university.grademanagement.model.FacultySubject;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Faculty to Subject mappings.
 */
public interface FacultySubjectDao {
    FacultySubject assign(int facultyId, int subjectId, int academicYearId, int semester);
    boolean unassign(int assignmentId);
    Optional<FacultySubject> findById(int id);
    List<FacultySubject> findByFaculty(int facultyId, Integer academicYearId);
    List<FacultySubject> findBySubject(int subjectId, Integer academicYearId);
    List<FacultySubject> findAll(Integer academicYearId);
    boolean isFacultyAssignedToSubject(int facultyId, int subjectId, int academicYearId);
}
