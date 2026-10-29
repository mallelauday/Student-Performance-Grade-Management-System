package com.university.grademanagement.service;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.dao.AuditLogDao;
import com.university.grademanagement.dao.AuditLogDaoImpl;
import com.university.grademanagement.dao.FacultyDao;
import com.university.grademanagement.dao.FacultyDaoImpl;
import com.university.grademanagement.dao.UserDao;
import com.university.grademanagement.dao.UserDaoImpl;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.exception.DuplicateRecordException;
import com.university.grademanagement.exception.ResourceNotFoundException;
import com.university.grademanagement.model.AuditLog;
import com.university.grademanagement.model.Faculty;
import com.university.grademanagement.model.Role;
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
 * Service managing faculty accounts and academic department profiles.
 */
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyDao facultyDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public FacultyService() {
        this(new FacultyDaoImpl(), new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public FacultyService(FacultyDao facultyDao, UserDao userDao, AuditLogDao auditLogDao) {
        this.facultyDao = facultyDao;
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    public List<Faculty> getAllFaculty(boolean activeOnly) {
        return facultyDao.findAll(activeOnly);
    }

    public Optional<Faculty> getFacultyById(int id) {
        return facultyDao.findById(id);
    }

    public Optional<Faculty> getFacultyByUserId(int userId) {
        return facultyDao.findByUserId(userId);
    }

    public Faculty createFaculty(Faculty faculty, String initialPassword, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        ValidationUtil.validateNotEmpty(faculty.getEmployeeId(), "Employee ID");
        ValidationUtil.validateNotEmpty(faculty.getFirstName(), "First Name");
        ValidationUtil.validateNotEmpty(faculty.getLastName(), "Last Name");
        ValidationUtil.validateEmail(faculty.getEmail());
        ValidationUtil.validateNotEmpty(faculty.getDepartment(), "Department");
        ValidationUtil.validateNotEmpty(faculty.getDesignation(), "Designation");

        if (initialPassword == null || initialPassword.length() < 6) {
            initialPassword = "Faculty@123";
        }

        if (facultyDao.findByEmployeeId(faculty.getEmployeeId()).isPresent()) {
            throw new DuplicateRecordException("Faculty with employee ID '" + faculty.getEmployeeId() + "' already exists.");
        }

        String username = faculty.getEmployeeId().trim().toLowerCase();
        if (userDao.findByUsername(username).isPresent()) {
            throw new DuplicateRecordException("User account for '" + username + "' already exists.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(PasswordUtil.hashPassword(initialPassword));
                user.setRole(Role.FACULTY);
                user.setFullName(faculty.getFullName());
                user.setEmail(faculty.getEmail());
                user.setPhone(faculty.getPhone());
                user.setActive(true);

                User createdUser = userDao.create(user, conn);

                faculty.setUserId(createdUser.getId());
                Faculty createdFaculty = facultyDao.create(faculty, conn);

                conn.commit();

                auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "CREATE_FACULTY",
                        "FACULTY", String.valueOf(createdFaculty.getId()),
                        "Registered faculty " + faculty.getEmployeeId() + " (" + faculty.getFullName() + ")"));

                return createdFaculty;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Failed to create faculty: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating faculty", e);
        }
    }

    public boolean updateFaculty(Faculty faculty, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);
        ValidationUtil.validateNotEmpty(faculty.getFirstName(), "First Name");
        ValidationUtil.validateNotEmpty(faculty.getLastName(), "Last Name");
        ValidationUtil.validateEmail(faculty.getEmail());

        boolean success = facultyDao.update(faculty);
        if (success) {
            userDao.findById(faculty.getUserId()).ifPresent(u -> {
                u.setFullName(faculty.getFullName());
                u.setEmail(faculty.getEmail());
                u.setPhone(faculty.getPhone());
                userDao.update(u);
            });

            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "UPDATE_FACULTY",
                    "FACULTY", String.valueOf(faculty.getId()),
                    "Updated faculty " + faculty.getEmployeeId()));
        }
        return success;
    }

    public boolean toggleFacultyStatus(int facultyId, boolean active, User currentUser) {
        AuthService.requireRole(currentUser, Role.ADMIN);

        Faculty f = facultyDao.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

        boolean success = facultyDao.setActive(facultyId, active);
        if (success) {
            userDao.setActive(f.getUserId(), active);
            String action = active ? "ACTIVATE_FACULTY" : "DEACTIVATE_FACULTY";
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), action,
                    "FACULTY", String.valueOf(facultyId),
                    "Faculty " + f.getEmployeeId() + " status set to " + (active ? "Active" : "Deactivated")));
        }
        return success;
    }
}
