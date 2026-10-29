package com.university.grademanagement.exception;

/**
 * Thrown when database errors, connection failures, or SQL exceptions occur.
 */
public class DatabaseException extends AppException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
