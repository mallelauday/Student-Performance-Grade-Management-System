package com.university.grademanagement.exception;

/**
 * Thrown when business rules or input validation fails.
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message);
    }
}
