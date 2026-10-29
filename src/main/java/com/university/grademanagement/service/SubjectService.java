package com.university.grademanagement.service;

import com.university.grademanagement.dao.*;
import com.university.grademanagement.exception.DuplicateRecordException;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.model.*;
import com.university.grademanagement.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service managing subjects, course configurations, and faculty assignments.
 */
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);

    private final SubjectDao subjectDao;
    private final FacultySubjectDao facultySubjectDao;
    private final AuditLogDao auditLogDao;

    public SubjectService() {
        this(new SubjectDaoImpl(), new FacultySubjectDaoImpl(), new AuditLogDaoImpl());
    }

    public SubjectService(SubjectDao subjectDao, FacultySubjectDao facultySubjectDao, AuditLogDao auditLogDao) {
        this.subjectDao = subjectDao;
        this.facultySubjectDao = facultySubjectDao;
        this.auditLogDao = auditLogDao;
    }

    public List<Subject> getAllSubjects(String dept, Integer sem, Boolean activeOnly) {
        return subjectDao.findAll(dept, sem, activeOnly);
    }

    public Optional<Subject> getSubjectById(int id) {
        return subjectDao.findById(id);
    }

    public Subject createSubject(Subject subject, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        ValidationUtil.validateNotEmpty(subject.getSubjectCode(), "Subject Code");
        ValidationUtil.validateNotEmpty(subject.getSubjectName(), "Subject Name");
        ValidationUtil.validateNotEmpty(subject.getDepartment(), "Department");
        ValidationUtil.validateSemester(subject.getSemester());
        ValidationUtil.validateCredits(subject.getCredits());

        if (subjectDao.findByCode(subject.getSubjectCode()).isPresent()) {
            throw new DuplicateRecordException("Subject code '" + subject.getSubjectCode() + "' already exists.");
        }

        Subject created = subjectDao.create(subject);
        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "CREATE_SUBJECT",
                "SUBJECT", String.valueOf(created.getId()),
                "Created subject " + subject.getSubjectCode() + " (" + subject.getSubjectName() + ")"));
        return created;
    }

    public boolean updateSubject(Subject subject, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        ValidationUtil.validateNotEmpty(subject.getSubjectName(), "Subject Name");
        ValidationUtil.validateNotEmpty(subject.getDepartment(), "Department");
        ValidationUtil.validateSemester(subject.getSemester());
        ValidationUtil.validateCredits(subject.getCredits());

        boolean success = subjectDao.update(subject);
        if (success) {
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "UPDATE_SUBJECT",
                    "SUBJECT", String.valueOf(subject.getId()),
                    "Updated subject " + subject.getSubjectCode()));
        }
        return success;
    }

    public boolean toggleSubjectStatus(int subjectId, boolean active, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);
        return subjectDao.setActive(subjectId, active);
    }

    public FacultySubject assignFaculty(int facultyId, int subjectId, int academicYearId, int semester, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);
        FacultySubject fs = facultySubjectDao.assign(facultyId, subjectId, academicYearId, semester);
        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "ASSIGN_FACULTY",
                "FACULTY_SUBJECT", String.valueOf(fs.getId()),
                "Assigned faculty ID " + facultyId + " to subject ID " + subjectId));
        return fs;
    }

    public boolean unassignFaculty(int assignmentId, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);
        boolean success = facultySubjectDao.unassign(assignmentId);
        if (success) {
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "UNASSIGN_FACULTY",
                    "FACULTY_SUBJECT", String.valueOf(assignmentId), "Removed faculty subject assignment"));
        }
        return success;
    }

    public List<FacultySubject> getFacultyAssignments(int facultyId, Integer academicYearId) {
        return facultySubjectDao.findByFaculty(facultyId, academicYearId);
    }

    public List<FacultySubject> getAllAssignments(Integer academicYearId) {
        return facultySubjectDao.findAll(academicYearId);
    }
}
