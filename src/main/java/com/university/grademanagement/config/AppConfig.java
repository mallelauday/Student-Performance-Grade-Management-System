package com.university.grademanagement.config;

/**
 * System-wide constants, academic policies, and application configuration defaults.
 */
public class AppConfig {
    public static final String APP_NAME = "Student Performance & Grade Management System";
    public static final String APP_VERSION = "1.0.0";
    public static final String UNIVERSITY_NAME = "Apex Institute of Technology & Sciences";
    public static final String ACCREDITATION = "Accredited by NAAC with 'A++' Grade | Approved by AICTE";

    // Academic grading constants
    public static final double PASS_PERCENTAGE_THRESHOLD = 40.0;
    public static final double DISTINCTION_PERCENTAGE_THRESHOLD = 75.0;
    public static final double FIRST_CLASS_PERCENTAGE_THRESHOLD = 60.0;

    // Default pagination and limits
    public static final int DEFAULT_PAGE_SIZE = 25;
    public static final int MAX_EXPORT_RECORDS = 5000;

    private AppConfig() {
    }
}
