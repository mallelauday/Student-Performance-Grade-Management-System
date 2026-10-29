package com.university.grademanagement.service;

import com.university.grademanagement.dao.*;
import com.university.grademanagement.exception.AuthorizationException;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.exception.ValidationException;
import com.university.grademanagement.model.*;
import com.university.grademanagement.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service orchestrating marks entry, modifications, validation, and role-based access checks.
 */
public class MarksService {

    private static final Logger logger = LoggerFactory.getLogger(MarksService.class);

    private final MarksDao marksDao;
    private final SubjectDao subjectDao;
    private final FacultyDao facultyDao;
    private final FacultySubjectDao facultySubjectDao;
    private final StudentDao studentDao;
    private final AuditLogDao auditLogDao;
    private final GradeService gradeService;

    public MarksService() {
        this(new MarksDaoImpl(), new SubjectDaoImpl(), new FacultyDaoImpl(),
                new FacultySubjectDaoImpl(), new StudentDaoImpl(), new AuditLogDaoImpl(), new GradeService());
    }

    public MarksService(MarksDao marksDao, SubjectDao subjectDao, FacultyDao facultyDao,
                        FacultySubjectDao facultySubjectDao, StudentDao studentDao, AuditLogDao auditLogDao, GradeService gradeService) {
        this.marksDao = marksDao;
        this.subjectDao = subjectDao;
        this.facultyDao = facultyDao;
        this.facultySubjectDao = facultySubjectDao;
        this.studentDao = studentDao;
        this.auditLogDao = auditLogDao;
        this.gradeService = gradeService;
    }

    /**
     * Verifies that the acting faculty member is authorized to manage marks for the specified subject.
     */
    private void verifyFacultyAssignment(int subjectId, int academicYearId, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admins have system-wide authority
        }

        if (currentUser.getRole() != Role.FACULTY) {
            throw new AuthorizationException("Only faculty and administrators can enter marks.");
        }

        Faculty faculty = facultyDao.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AuthorizationException("Faculty record not associated with this account."));

        boolean isAssigned = facultySubjectDao.isFacultyAssignedToSubject(faculty.getId(), subjectId, academicYearId);
        if (!isAssigned) {
            throw new AuthorizationException("Access Denied: You are not assigned to instruct or evaluate this subject.");
        }
    }

    /**
     * Saves or updates marks for an individual student with full validation and audit tracking.
     */
    public Marks saveMarks(Marks marks, User currentUser) {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }

        Subject subject = subjectDao.findById(marks.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + marks.getSubjectId()));

        // Security check
        verifyFacultyAssignment(subject.getId(), marks.getAcademicYearId(), currentUser);

        // Input validation
        ValidationUtil.validateMarks(
                marks.getInternalMarks(), subject.getMaxInternalMarks(),
                marks.getExternalMarks(), subject.getMaxExternalMarks()
        );

        double total = marks.getInternalMarks() + marks.getExternalMarks();
        marks.setTotalMarks(total);

        // Grade calculation
        boolean isPassed = gradeService.checkSubjectPassed(total, subject.getPassMarks(), marks.isAbsent());
        double percentage = subject.getMaxMarks() > 0 ? (total / subject.getMaxMarks()) * 100.0 : 0.0;
        String grade = gradeService.calculateGrade(percentage, isPassed, marks.isAbsent());
        double gradePoint = gradeService.calculateGradePoint(grade);

        marks.setPassed(isPassed);
        marks.setGrade(grade);
        marks.setGradePoint(gradePoint);
        marks.setEnteredByUserId(currentUser.getId());

        Marks saved = marksDao.saveOrUpdate(marks, null);

        // Log audit event
        String details = String.format("Marks recorded: Int=%.1f, Ext=%.1f, Total=%.1f, Grade=%s, Passed=%s",
                marks.getInternalMarks(), marks.getExternalMarks(), total, grade, isPassed);
        auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "SAVE_MARKS", "MARKS",
                String.valueOf(saved.getId()), details));

        return saved;
    }

    /**
     * Batch saves or updates marks for an entire class.
     */
    public boolean saveBatchMarks(List<Marks> marksList, int subjectId, int academicYearId, int semester, User currentUser) {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }

        Subject subject = subjectDao.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

        verifyFacultyAssignment(subjectId, academicYearId, currentUser);

        // Pre-validate all records
        for (Marks m : marksList) {
            ValidationUtil.validateMarks(
                    m.getInternalMarks(), subject.getMaxInternalMarks(),
                    m.getExternalMarks(), subject.getMaxExternalMarks()
            );

            double total = m.getInternalMarks() + m.getExternalMarks();
            m.setTotalMarks(total);
            m.setSubjectId(subjectId);
            m.setAcademicYearId(academicYearId);
            m.setSemester(semester);

            boolean isPassed = gradeService.checkSubjectPassed(total, subject.getPassMarks(), m.isAbsent());
            double percentage = subject.getMaxMarks() > 0 ? (total / subject.getMaxMarks()) * 100.0 : 0.0;
            String grade = gradeService.calculateGrade(percentage, isPassed, m.isAbsent());
            double gradePoint = gradeService.calculateGradePoint(grade);

            m.setPassed(isPassed);
            m.setGrade(grade);
            m.setGradePoint(gradePoint);
            m.setEnteredByUserId(currentUser.getId());
        }

        boolean success = marksDao.saveBatch(marksList, currentUser.getId());

        if (success) {
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "BATCH_MARKS_SAVE", "MARKS",
                    "Subject:" + subject.getSubjectCode(), "Batch marks saved for " + marksList.size() + " students"));
        }
        return success;
    }

    /**
     * Fetches marks for a student, enforcing student privacy rules.
     */
    public List<Marks> getMarksForStudent(int studentId, int academicYearId, int semester, User currentUser) {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }

        if (currentUser.getRole() == Role.STUDENT) {
            Student student = studentDao.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            if (student.getUserId() != currentUser.getId()) {
                throw new AuthorizationException("Security Alert: Students are strictly permitted to view only their own marks.");
            }
        }

        return marksDao.findByStudentAndSemester(studentId, academicYearId, semester);
    }

    /**
     * Fetches marks for a subject, verifying faculty assignment.
     */
    public List<Marks> getMarksForSubject(int subjectId, int academicYearId, int semester, User currentUser) {
        if (currentUser == null) {
            throw new AuthorizationException("Authentication required.");
        }
        verifyFacultyAssignment(subjectId, academicYearId, currentUser);
        return marksDao.findBySubjectAndSemester(subjectId, academicYearId, semester);
    }
}
