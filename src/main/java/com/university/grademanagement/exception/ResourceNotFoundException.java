package com.university.grademanagement.exception;

/**
 * Thrown when an expected entity (Student, Subject, User, etc.) cannot be found.
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
