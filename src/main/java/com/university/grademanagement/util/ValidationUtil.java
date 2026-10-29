package com.university.grademanagement.util;

import com.university.grademanagement.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility for comprehensive validation of input data, roll numbers, emails, and marks.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_-]{3,20}$"
    );

    private ValidationUtil() {
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank.");
        }
    }

    public static void validateEmail(String email) {
        validateNotEmpty(email, "Email address");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validateRollNumber(String rollNumber) {
        validateNotEmpty(rollNumber, "Roll number");
        if (!ROLL_NUMBER_PATTERN.matcher(rollNumber.trim()).matches()) {
            throw new ValidationException("Roll number can only contain letters, digits, hyphens or underscores (3-20 characters).");
        }
    }

    public static void validateSemester(int semester) {
        if (semester < 1 || semester > 8) {
            throw new ValidationException("Semester must be between 1 and 8.");
        }
    }

    public static void validateCredits(int credits) {
        if (credits < 1 || credits > 10) {
            throw new ValidationException("Credits must be between 1 and 10.");
        }
    }

    public static void validateMarks(double internalMarks, double maxInternal, double externalMarks, double maxExternal) {
        if (internalMarks < 0) {
            throw new ValidationException("Internal marks cannot be negative.");
        }
        if (internalMarks > maxInternal) {
            throw new ValidationException(String.format("Internal marks (%.2f) cannot exceed maximum internal marks (%.2f).", internalMarks, maxInternal));
        }
        if (externalMarks < 0) {
            throw new ValidationException("External marks cannot be negative.");
        }
        if (externalMarks > maxExternal) {
            throw new ValidationException(String.format("External marks (%.2f) cannot exceed maximum external marks (%.2f).", externalMarks, maxExternal));
        }
    }
}
