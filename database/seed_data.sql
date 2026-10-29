-- ============================================================================
-- Student Performance and Grade Management System
-- Seed Data Script
-- ============================================================================

USE student_grade_db;

-- Clear previous data in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE marks;
TRUNCATE TABLE faculty_subjects;
TRUNCATE TABLE subjects;
TRUNCATE TABLE faculties;
TRUNCATE TABLE students;
TRUNCATE TABLE users;
TRUNCATE TABLE academic_years;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Academic Years
INSERT INTO academic_years (id, year_code, year_name, is_current) VALUES
(1, '2024-2025', 'Academic Year 2024-2025', FALSE),
(2, '2025-2026', 'Academic Year 2025-2026', TRUE);

-- 2. Users
-- Passwords:
-- Admin: Admin@123   ($2a$10$Snz9mlS/Gdqcev7PxP3afuR5S4MDPI7O0FefpNYsYjt7Q02SYH7uK)
-- Faculty: Faculty@123 ($2a$10$QAdUgHo7z1KYn2lXWMjC7uPCA7j.u4gPaX0H1AJPeZ0obZMlJEXzi)
-- Student: Student@123 ($2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W)

INSERT INTO users (id, username, password_hash, role, full_name, email, phone, is_active) VALUES
-- Admin
(1, 'admin', '$2a$10$Snz9mlS/Gdqcev7PxP3afuR5S4MDPI7O0FefpNYsYjt7Q02SYH7uK', 'ADMIN', 'System Administrator', 'admin@university.edu', '+91 9876543210', TRUE),

-- Faculty
(2, 'dr.sharma', '$2a$10$QAdUgHo7z1KYn2lXWMjC7uPCA7j.u4gPaX0H1AJPeZ0obZMlJEXzi', 'FACULTY', 'Dr. Ramesh Sharma', 'r.sharma@university.edu', '+91 9876543211', TRUE),
(3, 'prof.patel', '$2a$10$QAdUgHo7z1KYn2lXWMjC7uPCA7j.u4gPaX0H1AJPeZ0obZMlJEXzi', 'FACULTY', 'Prof. Priya Patel', 'p.patel@university.edu', '+91 9876543212', TRUE),
(4, 'dr.verma', '$2a$10$QAdUgHo7z1KYn2lXWMjC7uPCA7j.u4gPaX0H1AJPeZ0obZMlJEXzi', 'FACULTY', 'Dr. Alok Verma', 'a.verma@university.edu', '+91 9876543213', TRUE),

-- Students
(5, 'cs2023001', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Aarav Sharma', 'aarav.cs23@university.edu', '+91 9123456701', TRUE),
(6, 'cs2023002', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Diya Patel', 'diya.cs23@university.edu', '+91 9123456702', TRUE),
(7, 'cs2023003', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Rohan Gupta', 'rohan.cs23@university.edu', '+91 9123456703', TRUE),
(8, 'cs2023004', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Sneha Reddy', 'sneha.cs23@university.edu', '+91 9123456704', TRUE),
(9, 'cs2023005', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Vikram Singh', 'vikram.cs23@university.edu', '+91 9123456705', TRUE),
(10, 'cs2023006', '$2a$10$/YDeQ3ag/t2Y9K0heIDOTes5WdWrzKllm4SqtrGpxFDyoaozORz7W', 'STUDENT', 'Ananya Roy', 'ananya.cs23@university.edu', '+91 9123456706', TRUE);

-- 3. Faculties
INSERT INTO faculties (id, user_id, employee_id, first_name, last_name, department, designation, qualification, email, phone, is_active) VALUES
(1, 2, 'FAC-CSE-001', 'Ramesh', 'Sharma', 'Computer Science & Engineering', 'Professor & HOD', 'Ph.D. in Computer Science', 'r.sharma@university.edu', '+91 9876543211', TRUE),
(2, 3, 'FAC-CSE-002', 'Priya', 'Patel', 'Computer Science & Engineering', 'Associate Professor', 'M.Tech, Ph.D. (Pursuing)', 'p.patel@university.edu', '+91 9876543212', TRUE),
(3, 4, 'FAC-CSE-003', 'Alok', 'Verma', 'Computer Science & Engineering', 'Assistant Professor', 'Ph.D. in Information Technology', 'a.verma@university.edu', '+91 9876543213', TRUE);

-- 4. Students
INSERT INTO students (id, user_id, roll_number, first_name, last_name, gender, date_of_birth, email, phone, department, current_semester, admission_year, address, guardian_name, guardian_contact, is_active) VALUES
(1, 5, 'CS2023001', 'Aarav', 'Sharma', 'MALE', '2004-05-14', 'aarav.cs23@university.edu', '+91 9123456701', 'Computer Science & Engineering', 5, 2023, '12 Park Street, Bangalore', 'Rajesh Sharma', '+91 9811122233', TRUE),
(2, 6, 'CS2023002', 'Diya', 'Patel', 'FEMALE', '2004-08-22', 'diya.cs23@university.edu', '+91 9123456702', 'Computer Science & Engineering', 5, 2023, '45 Green Glen, Ahmedabad', 'Mukesh Patel', '+91 9822233344', TRUE),
(3, 7, 'CS2023003', 'Rohan', 'Gupta', 'MALE', '2004-01-10', 'rohan.cs23@university.edu', '+91 9123456703', 'Computer Science & Engineering', 5, 2023, '78 Civil Lines, Delhi', 'Suresh Gupta', '+91 9833344455', TRUE),
(4, 8, 'CS2023004', 'Sneha', 'Reddy', 'FEMALE', '2004-11-30', 'sneha.cs23@university.edu', '+91 9123456704', 'Computer Science & Engineering', 5, 2023, '102 Jubilee Hills, Hyderabad', 'Venkat Reddy', '+91 9844455566', TRUE),
(5, 9, 'CS2023005', 'Vikram', 'Singh', 'MALE', '2003-12-05', 'vikram.cs23@university.edu', '+91 9123456705', 'Computer Science & Engineering', 5, 2023, '89 Mall Road, Jaipur', 'Karan Singh', '+91 9855566677', TRUE),
(6, 10, 'CS2023006', 'Ananya', 'Roy', 'FEMALE', '2004-03-19', 'ananya.cs23@university.edu', '+91 9123456706', 'Computer Science & Engineering', 5, 2023, '54 Salt Lake, Kolkata', 'Debasish Roy', '+91 9866677788', TRUE);

-- 5. Subjects
INSERT INTO subjects (id, subject_code, subject_name, department, semester, credits, max_internal_marks, max_external_marks, max_marks, pass_marks, is_elective, is_active) VALUES
(1, 'CS501', 'Operating Systems', 'Computer Science & Engineering', 5, 4, 40.0, 60.0, 100.0, 40.0, FALSE, TRUE),
(2, 'CS502', 'Database Management Systems', 'Computer Science & Engineering', 5, 4, 40.0, 60.0, 100.0, 40.0, FALSE, TRUE),
(3, 'CS503', 'Computer Networks', 'Computer Science & Engineering', 5, 4, 40.0, 60.0, 100.0, 40.0, FALSE, TRUE),
(4, 'CS504', 'Software Engineering', 'Computer Science & Engineering', 5, 3, 40.0, 60.0, 100.0, 40.0, FALSE, TRUE),
(5, 'CS505', 'Artificial Intelligence', 'Computer Science & Engineering', 5, 3, 40.0, 60.0, 100.0, 40.0, FALSE, TRUE);

-- 6. Faculty Subject Assignments
INSERT INTO faculty_subjects (id, faculty_id, subject_id, academic_year_id, semester) VALUES
(1, 1, 1, 2, 5), -- Dr. Sharma -> CS501 Operating Systems
(2, 1, 4, 2, 5), -- Dr. Sharma -> CS504 Software Engineering
(3, 2, 2, 2, 5), -- Prof. Patel -> CS502 DBMS
(4, 3, 3, 2, 5), -- Dr. Verma -> CS503 Computer Networks
(5, 3, 5, 2, 5); -- Dr. Verma -> CS505 Artificial Intelligence

-- 7. Marks Records (Semester 5, Academic Year 2025-2026)
-- Grading Scale:
-- 90-100: A+ (10.0), 80-89.99: A (9.0), 70-79.99: B (8.0), 60-69.99: C (7.0), 50-59.99: D (6.0), 40-49.99: E (5.0), <40: F (0.0)

INSERT INTO marks (student_id, subject_id, academic_year_id, semester, internal_marks, external_marks, total_marks, grade, grade_point, is_passed, is_absent, remarks, entered_by_user_id) VALUES
-- Student 1 (Aarav Sharma - Top performer, High A+)
(1, 1, 2, 5, 38.0, 56.0, 94.0, 'A+', 10.0, TRUE, FALSE, 'Exceptional performance', 2),
(1, 2, 2, 5, 37.0, 55.0, 92.0, 'A+', 10.0, TRUE, FALSE, 'Excellent understanding', 3),
(1, 3, 2, 5, 36.0, 54.0, 90.0, 'A+', 10.0, TRUE, FALSE, 'Outstanding analytical skills', 4),
(1, 4, 2, 5, 39.0, 57.0, 96.0, 'A+', 10.0, TRUE, FALSE, 'Superb project design', 2),
(1, 5, 2, 5, 38.0, 53.0, 91.0, 'A+', 10.0, TRUE, FALSE, 'Innovative solutions', 4),

-- Student 2 (Diya Patel - Second Rank, Consistent A/A+)
(2, 1, 2, 5, 36.0, 51.0, 87.0, 'A', 9.0, TRUE, FALSE, 'Very good concepts', 2),
(2, 2, 2, 5, 35.0, 53.0, 88.0, 'A', 9.0, TRUE, FALSE, 'Thorough queries', 3),
(2, 3, 2, 5, 37.0, 54.0, 91.0, 'A+', 10.0, TRUE, FALSE, 'Excellent networking knowledge', 4),
(2, 4, 2, 5, 34.0, 52.0, 86.0, 'A', 9.0, TRUE, FALSE, 'Strong software models', 2),
(2, 5, 2, 5, 35.0, 50.0, 85.0, 'A', 9.0, TRUE, FALSE, 'Well executed lab tasks', 4),

-- Student 3 (Rohan Gupta - Good student, Solid B+/A)
(3, 1, 2, 5, 31.0, 46.0, 77.0, 'B', 8.0, TRUE, FALSE, 'Good effort', 2),
(3, 2, 2, 5, 32.0, 48.0, 80.0, 'A', 9.0, TRUE, FALSE, 'Good grasp of SQL', 3),
(3, 3, 2, 5, 29.0, 45.0, 74.0, 'B', 8.0, TRUE, FALSE, 'Consistent participation', 4),
(3, 4, 2, 5, 30.0, 47.0, 77.0, 'B', 8.0, TRUE, FALSE, 'Sound documentation', 2),
(3, 5, 2, 5, 31.0, 48.0, 79.0, 'B', 8.0, TRUE, FALSE, 'Good algorithmic skills', 4),

-- Student 4 (Sneha Reddy - Average student, C/B range)
(4, 1, 2, 5, 27.0, 40.0, 67.0, 'C', 7.0, TRUE, FALSE, 'Fair performance', 2),
(4, 2, 2, 5, 28.0, 43.0, 71.0, 'B', 8.0, TRUE, FALSE, 'Fair understanding', 3),
(4, 3, 2, 5, 25.0, 39.0, 64.0, 'C', 7.0, TRUE, FALSE, 'Needs deeper practice in subnetting', 4),
(4, 4, 2, 5, 29.0, 41.0, 70.0, 'B', 8.0, TRUE, FALSE, 'Active in team work', 2),
(4, 5, 2, 5, 26.0, 38.0, 64.0, 'C', 7.0, TRUE, FALSE, 'Satisfactory lab results', 4),

-- Student 5 (Vikram Singh - At-risk / Academic support needed, borderline & one fail)
(5, 1, 2, 5, 18.0, 26.0, 44.0, 'E', 5.0, TRUE, FALSE, 'Marginal pass, requires tutoring', 2),
(5, 2, 2, 5, 20.0, 31.0, 51.0, 'D', 6.0, TRUE, FALSE, 'Basic concepts clear', 3),
(5, 3, 2, 5, 14.0, 21.0, 35.0, 'F', 0.0, FALSE, FALSE, 'Failed subject. Requires supplementary exam', 4),
(5, 4, 2, 5, 19.0, 27.0, 46.0, 'E', 5.0, TRUE, FALSE, 'Needs guidance on testing', 2),
(5, 5, 2, 5, 20.0, 28.0, 48.0, 'E', 5.0, TRUE, FALSE, 'Low test scores', 4),

-- Student 6 (Ananya Roy - High B/A student)
(6, 1, 2, 5, 33.0, 49.0, 82.0, 'A', 9.0, TRUE, FALSE, 'Active in discussions', 2),
(6, 2, 2, 5, 34.0, 50.0, 84.0, 'A', 9.0, TRUE, FALSE, 'Very organized queries', 3),
(6, 3, 2, 5, 32.0, 47.0, 79.0, 'B', 8.0, TRUE, FALSE, 'Good protocol understanding', 4),
(6, 4, 2, 5, 35.0, 48.0, 83.0, 'A', 9.0, TRUE, FALSE, 'Clean architectural design', 2),
(6, 5, 2, 5, 33.0, 48.0, 81.0, 'A', 9.0, TRUE, FALSE, 'Strong implementation', 4);


-- 8. Audit Logs
INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address) VALUES
(1, 'admin', 'SYSTEM_INIT', 'SYSTEM', '1', 'Initial database schema and academic year configuration created', '127.0.0.1'),
(1, 'admin', 'ASSIGN_FACULTY', 'FACULTY_SUBJECT', '1', 'Assigned Dr. Ramesh Sharma to CS501 Operating Systems for 2025-2026', '127.0.0.1'),
(2, 'dr.sharma', 'ENTER_MARKS', 'MARKS', '1', 'Entered internal and external marks for Semester 5 CS501', '127.0.0.1'),
(3, 'prof.patel', 'ENTER_MARKS', 'MARKS', '2', 'Entered internal and external marks for Semester 5 CS502', '127.0.0.1'),
(4, 'dr.verma', 'ENTER_MARKS', 'MARKS', '3', 'Entered internal and external marks for Semester 5 CS503', '127.0.0.1');
