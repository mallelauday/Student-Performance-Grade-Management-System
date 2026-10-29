package com.university.grademanagement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt password hashing and verification.
 */
public class PasswordUtil {

    private static final int WORKLOAD = 10;

    private PasswordUtil() {
        // Private constructor for utility class
    }

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * @param plainTextPassword plain text password
     * @return BCrypt salted hash string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORKLOAD));
    }

    /**
     * Verifies a plain text password against a stored BCrypt hash.
     *
     * @param plainTextPassword plain text password entered by user
     * @param storedHash        BCrypt hash from database
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        if (plainTextPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Malformed hash
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Admin@123: " + hashPassword("Admin@123"));
        System.out.println("Faculty@123: " + hashPassword("Faculty@123"));
        System.out.println("Student@123: " + hashPassword("Student@123"));
    }
}
