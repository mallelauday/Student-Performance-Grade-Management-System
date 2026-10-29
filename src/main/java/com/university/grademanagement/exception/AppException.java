package com.university.grademanagement.exception;

/**
 * Base unchecked exception for the Student Performance and Grade Management System.
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
