package com.university.grademanagement.service;

import com.university.grademanagement.dao.AuditLogDao;
import com.university.grademanagement.dao.AuditLogDaoImpl;
import com.university.grademanagement.dao.UserDao;
import com.university.grademanagement.dao.UserDaoImpl;
import com.university.grademanagement.exception.AuthenticationException;
import com.university.grademanagement.exception.AuthorizationException;
import com.university.grademanagement.exception.ValidationException;
import com.university.grademanagement.model.AuditLog;
import com.university.grademanagement.model.Role;
import com.university.grademanagement.model.User;
import com.university.grademanagement.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service managing user authentication, sessions, role authorization, and password management.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    // Active session user
    private static User currentUser;

    public AuthService() {
        this(new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public AuthService(UserDao userDao, AuditLogDao auditLogDao) {
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Authenticates a user with username and password, updating login timestamp and audit trail.
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter both username and password.");
        }

        String cleanUsername = username.trim();
        Optional<User> userOpt = userDao.findByUsername(cleanUsername);

        // Security practice: use a generic error message to prevent username enumeration attacks
        if (userOpt.isEmpty()) {
            auditLogDao.log(new AuditLog(null, cleanUsername, "LOGIN_FAILED", "USER", null, "Authentication failed: invalid credentials"));
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            auditLogDao.log(new AuditLog(user.getId(), cleanUsername, "LOGIN_BLOCKED", "USER", String.valueOf(user.getId()), "Account is deactivated"));
            throw new AuthenticationException("Your account is deactivated. Please contact the administrator.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            auditLogDao.log(new AuditLog(user.getId(), cleanUsername, "LOGIN_FAILED", "USER", String.valueOf(user.getId()), "Authentication failed: invalid password"));
            throw new AuthenticationException("Invalid username or password.");
        }

        // Login success
        userDao.updateLastLogin(user.getId());
        currentUser = user;
        auditLogDao.log(new AuditLog(user.getId(), user.getUsername(), "LOGIN_SUCCESS", "USER", String.valueOf(user.getId()), "User logged in with role " + user.getRole()));

        logger.info("User '{}' ({}) logged in successfully.", user.getUsername(), user.getRole());
        return user;
    }

    /**
     * Logs out the current user and clears session state.
     */
    public void logout() {
        if (currentUser != null) {
            auditLogDao.log(new AuditLog(currentUser.getId(), currentUser.getUsername(), "LOGOUT", "USER", String.valueOf(currentUser.getId()), "User logged out"));
            logger.info("User '{}' logged out.", currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Gets the currently authenticated user session.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Explicitly sets session user (primarily for testing or context switching).
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Changes password after verifying the old password.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword, User actingUser) {
        if (actingUser == null) {
            throw new AuthorizationException("Authentication required.");
        }
        if (actingUser.getId() != userId && actingUser.getRole() != Role.ADMIN) {
            throw new AuthorizationException("You are not authorized to change another user's password.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long.");
        }

        User targetUser = userDao.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User account not found."));

        // If user is changing their own password, verify old password
        if (actingUser.getId() == userId) {
            if (!PasswordUtil.checkPassword(oldPassword, targetUser.getPasswordHash())) {
                throw new AuthenticationException("Incorrect existing password.");
            }
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean success = userDao.updatePassword(userId, newHash);
        if (success) {
            auditLogDao.log(new AuditLog(actingUser.getId(), actingUser.getUsername(), "PASSWORD_CHANGE", "USER", String.valueOf(userId), "Password updated successfully"));
        }
        return success;
    }

    /**
     * Asserts that the acting user has one of the required roles.
     */
    public static void requireRole(User user, Role... allowedRoles) {
        if (user == null) {
            throw new AuthorizationException("Authentication required. Please log in.");
        }
        for (Role role : allowedRoles) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new AuthorizationException("Access denied. Required role: " + java.util.Arrays.toString(allowedRoles));
    }
}
