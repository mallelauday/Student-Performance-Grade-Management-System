package com.university.grademanagement.exception;

/**
 * Thrown when user credentials or authentication state is invalid.
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message);
    }
}
