-- ============================================================================
-- Student Performance and Grade Management System
-- Database Schema Script (MySQL 8.0+)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS student_grade_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_grade_db;

-- 1. Academic Years / Periods
CREATE TABLE IF NOT EXISTS academic_years (
    id INT AUTO_INCREMENT PRIMARY KEY,
    year_code VARCHAR(20) NOT NULL UNIQUE,       -- e.g. "2024-2025", "2025-2026"
    year_name VARCHAR(100) NOT NULL,             -- e.g. "Academic Year 2025-2026"
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_year_current (is_current)
) ENGINE=InnoDB;

-- 2. System Users (Authentication & Roles)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'FACULTY', 'STUDENT') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_active (is_active)
) ENGINE=InnoDB;

-- 3. Students
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL DEFAULT 'MALE',
    date_of_birth DATE NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(60) NOT NULL,
    current_semester INT NOT NULL CHECK (current_semester BETWEEN 1 AND 8),
    admission_year INT NOT NULL,
    address TEXT,
    guardian_name VARCHAR(100),
    guardian_contact VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_student_dept_sem (department, current_semester),
    INDEX idx_student_roll (roll_number)
) ENGINE=InnoDB;

-- 4. Faculties
CREATE TABLE IF NOT EXISTS faculties (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_id VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(60) NOT NULL,
    designation VARCHAR(60) NOT NULL,
    qualification VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_faculties_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_faculty_dept (department)
) ENGINE=InnoDB;

-- 5. Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    department VARCHAR(60) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    credits INT NOT NULL CHECK (credits BETWEEN 1 AND 6),
    max_internal_marks DOUBLE NOT NULL DEFAULT 40.0,
    max_external_marks DOUBLE NOT NULL DEFAULT 60.0,
    max_marks DOUBLE NOT NULL DEFAULT 100.0,
    pass_marks DOUBLE NOT NULL DEFAULT 40.0,
    is_elective BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subject_dept_sem (department, semester)
) ENGINE=InnoDB;

-- 6. Faculty Subject Assignments
CREATE TABLE IF NOT EXISTS faculty_subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    faculty_id INT NOT NULL,
    subject_id INT NOT NULL,
    academic_year_id INT NOT NULL,
    semester INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_faculty_subject_year UNIQUE (faculty_id, subject_id, academic_year_id, semester),
    CONSTRAINT fk_fs_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE,
    CONSTRAINT fk_fs_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fs_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 7. Student Marks
CREATE TABLE IF NOT EXISTS marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    academic_year_id INT NOT NULL,
    semester INT NOT NULL,
    internal_marks DOUBLE NOT NULL DEFAULT 0.0,
    external_marks DOUBLE NOT NULL DEFAULT 0.0,
    total_marks DOUBLE NOT NULL DEFAULT 0.0,
    grade VARCHAR(5) NOT NULL,
    grade_point DOUBLE NOT NULL DEFAULT 0.0,
    is_passed BOOLEAN NOT NULL DEFAULT FALSE,
    is_absent BOOLEAN NOT NULL DEFAULT FALSE,
    remarks VARCHAR(255),
    entered_by_user_id INT NULL,
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_subject_period UNIQUE (student_id, subject_id, academic_year_id, semester),
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_marks_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT fk_marks_entered_by FOREIGN KEY (entered_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_marks_student_sem (student_id, semester),
    INDEX idx_marks_subject (subject_id)
) ENGINE=InnoDB;

-- 8. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    details TEXT,
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_time (timestamp),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB;
