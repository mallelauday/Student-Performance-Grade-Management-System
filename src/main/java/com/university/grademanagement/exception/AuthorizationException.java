package com.university.grademanagement.exception;

/**
 * Thrown when an authenticated user attempts an operation outside their authorized role or scope.
 */
public class AuthorizationException extends AppException {
    public AuthorizationException(String message) {
        super(message);
    }
}
