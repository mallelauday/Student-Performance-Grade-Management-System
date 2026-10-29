package com.university.grademanagement.exception;

/**
 * Thrown when attempting to insert or update a record that violates uniqueness constraints.
 */
public class DuplicateRecordException extends AppException {
    public DuplicateRecordException(String message) {
        super(message);
    }
}
