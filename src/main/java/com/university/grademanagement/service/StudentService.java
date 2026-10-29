package com.university.grademanagement.service;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.dao.AuditLogDao;
import com.university.grademanagement.dao.AuditLogDaoImpl;
import com.university.grademanagement.dao.StudentDao;
import com.university.grademanagement.dao.StudentDaoImpl;
import com.university.grademanagement.dao.UserDao;
import com.university.grademanagement.dao.UserDaoImpl;
import com.university.grademanagement.exception.AuthorizationException;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.exception.DuplicateRecordException;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.model.AuditLog;
import com.university.grademanagement.model.Role;
import com.university.grademanagement.model.Student;
import com.university.grademanagement.model.User;
import com.university.grademanagement.util.PasswordUtil;
import com.university.grademanagement.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing student profile administration, transactions, and role authorization.
 */
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentDao studentDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public StudentService() {
        this(new StudentDaoImpl(), new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public StudentService(StudentDao studentDao, UserDao userDao, AuditLogDao auditLogDao) {
        this.studentDao = studentDao;
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    public Optional<Student> getStudentById(int id, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN, Role.FACULTY, Role.STUDENT);
        Optional<Student> student = studentDao.findById(id);

        // If current user is student, ensure they can only query their own record
        if (currentUser.getRole() == Role.STUDENT && student.isPresent()) {
            if (student.get().getUserId() != currentUser.getId()) {
                throw new AuthorizationException("Students can only view their own profile.");
            }
        }
        return student;
    }

    public Optional<Student> getStudentByUserId(int userId) {
        return studentDao.findByUserId(userId);
    }

    public Optional<Student> getStudentByRollNumber(String rollNumber) {
        return studentDao.findByRollNumber(rollNumber);
    }

    public List<Student> getAllStudents(String search, String dept, Integer sem, Boolean activeOnly, int offset, int limit, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN, Role.FACULTY);
        return studentDao.findAll(search, dept, sem, activeOnly, offset, limit);
    }

    public int countStudents(String search, String dept, Integer sem, Boolean activeOnly) {
        return studentDao.count(search, dept, sem, activeOnly);
    }

    public List<String> getDistinctDepartments() {
        return studentDao.getDistinctDepartments();
    }

    /**
     * Creates a new Student and associated User login account within a single database transaction.
     */
    public Student createStudent(Student student, String initialPassword, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        // Validations
        ValidationUtil.validateRollNumber(student.getRollNumber());
        ValidationUtil.validateNotEmpty(student.getFirstName(), "First Name");
        ValidationUtil.validateNotEmpty(student.getLastName(), "Last Name");
        ValidationUtil.validateEmail(student.getEmail());
        ValidationUtil.validateNotEmpty(student.getDepartment(), "Department");
        ValidationUtil.validateSemester(student.getCurrentSemester());

        if (initialPassword == null || initialPassword.length() < 6) {
            initialPassword = "Student@123"; // Standard default initial password
        }

        // Duplicate checks
        if (studentDao.existsByRollNumber(student.getRollNumber(), null)) {
            throw new DuplicateRecordException("A student with roll number '" + student.getRollNumber() + "' already exists.");
        }
        String username = student.getRollNumber().trim().toLowerCase();
        if (userDao.findByUsername(username).isPresent()) {
            throw new DuplicateRecordException("User account for '" + username + "' already exists.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create User record
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPasswordHash(PasswordUtil.hashPassword(initialPassword));
                newUser.setRole(Role.STUDENT);
                newUser.setFullName(student.getFirstName() + " " + student.getLastName());
                newUser.setEmail(student.getEmail());
                newUser.setPhone(student.getPhone());
                newUser.setActive(true);

                User createdUser = userDao.create(newUser, conn);

                // 2. Create Student record
                student.setUserId(createdUser.getId());
                Student createdStudent = studentDao.create(student, conn);

                conn.commit();

                // Audit log
                auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "CREATE_STUDENT",
                        "STUDENT", String.valueOf(createdStudent.getId()),
                        "Registered new student " + student.getRollNumber() + " (" + student.getFullName() + ")"));

                return createdStudent;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Transaction rolled back while creating student: {}", e.getMessage());
                throw new DatabaseException("Failed to register student: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error while creating student", e);
        }
    }

    /**
     * Updates student personal and academic details.
     */
    public boolean updateStudent(Student student, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        ValidationUtil.validateNotEmpty(student.getFirstName(), "First Name");
        ValidationUtil.validateNotEmpty(student.getLastName(), "Last Name");
        ValidationUtil.validateEmail(student.getEmail());
        ValidationUtil.validateSemester(student.getCurrentSemester());

        boolean success = studentDao.update(student);
        if (success) {
            // Also keep user account details in sync
            userDao.findById(student.getUserId()).ifPresent(u -> {
                u.setFullName(student.getFullName());
                u.setEmail(student.getEmail());
                u.setPhone(student.getPhone());
                userDao.update(u);
            });

            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "UPDATE_STUDENT",
                    "STUDENT", String.valueOf(student.getId()),
                    "Updated profile for student " + student.getRollNumber()));
        }
        return success;
    }

    /**
     * Safely deactivates or activates a student account without deleting academic history.
     */
    public boolean toggleStudentStatus(int studentId, boolean active, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        Student s = studentDao.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        boolean success = studentDao.setActive(studentId, active);
        if (success) {
            userDao.setActive(s.getUserId(), active);
            String action = active ? "ACTIVATE_STUDENT" : "DEACTIVATE_STUDENT";
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), action,
                    "STUDENT", String.valueOf(studentId),
                    "Student " + s.getRollNumber() + " status set to " + (active ? "Active" : "Deactivated")));
        }
        return success;
    }
}
