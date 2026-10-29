package com.university.grademanagement.service;

import com.university.grademanagement.exception.ValidationException;
import com.university.grademanagement.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating grading policy, grade point calculation, and validation constraints.
 */
public class GradeServiceTest {

    private GradeService gradeService;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService(null, null, null, null);
    }

    @Test
    @DisplayName("Should correctly evaluate letter grades based on percentage")
    void testGradeCalculation() {
        assertEquals("A+", gradeService.calculateGrade(95.0, true, false));
        assertEquals("A+", gradeService.calculateGrade(90.0, true, false));
        assertEquals("A", gradeService.calculateGrade(89.5, true, false));
        assertEquals("A", gradeService.calculateGrade(80.0, true, false));
        assertEquals("B", gradeService.calculateGrade(75.0, true, false));
        assertEquals("B", gradeService.calculateGrade(70.0, true, false));
        assertEquals("C", gradeService.calculateGrade(65.0, true, false));
        assertEquals("D", gradeService.calculateGrade(55.0, true, false));
        assertEquals("E", gradeService.calculateGrade(40.0, true, false));
        assertEquals("F", gradeService.calculateGrade(39.9, false, false));
    }

    @Test
    @DisplayName("Should assign grade F and 0.0 points when absent or failed")
    void testAbsentAndFailedStudent() {
        assertEquals("F", gradeService.calculateGrade(85.0, true, true)); // Absent
        assertEquals("F", gradeService.calculateGrade(85.0, false, false)); // Failed subject
        assertEquals(0.0, gradeService.calculateGradePoint("F"));
    }

    @Test
    @DisplayName("Should correctly convert letter grades to 10-point scale grade points")
    void testGradePoints() {
        assertEquals(10.0, gradeService.calculateGradePoint("A+"));
        assertEquals(9.0, gradeService.calculateGradePoint("A"));
        assertEquals(8.0, gradeService.calculateGradePoint("B"));
        assertEquals(7.0, gradeService.calculateGradePoint("C"));
        assertEquals(6.0, gradeService.calculateGradePoint("D"));
        assertEquals(5.0, gradeService.calculateGradePoint("E"));
        assertEquals(0.0, gradeService.calculateGradePoint("F"));
    }

    @Test
    @DisplayName("Should validate marks boundaries and prevent invalid values")
    void testMarksValidation() {
        // Valid
        assertDoesNotThrow(() -> ValidationUtil.validateMarks(35.0, 40.0, 50.0, 60.0));

        // Negative internal marks
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMarks(-5.0, 40.0, 50.0, 60.0));

        // Negative external marks
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMarks(30.0, 40.0, -10.0, 60.0));

        // Internal exceeds max
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMarks(45.0, 40.0, 50.0, 60.0));

        // External exceeds max
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMarks(30.0, 40.0, 65.0, 60.0));
    }

    @Test
    @DisplayName("Should validate roll numbers correctly")
    void testRollNumberValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateRollNumber("CS2023001"));
        assertDoesNotThrow(() -> ValidationUtil.validateRollNumber("CS-2023-01"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRollNumber(""));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRollNumber("A")); // Too short
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRollNumber("CS 2023 001")); // Has space
    }
}
