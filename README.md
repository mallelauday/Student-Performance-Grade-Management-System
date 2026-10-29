# Student Performance and Grade Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![GUI](https://img.shields.io/badge/GUI-Java%20Swing-orange.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-00758F.svg)](https://www.mysql.com/)
[![Build](https://img.shields.io/badge/Build-Maven-C71A36.svg)](https://maven.apache.org/)
[![Theme](https://img.shields.io/badge/Theme-FlatLaf%20Modern-5C6BC0.svg)](https://www.formdev.com/flatlaf/)

An enterprise-grade, desktop-based academic performance and grade evaluation management system developed in **Java 17+ (Swing)** and backed by **MySQL 8.0**. Built with a layered object-oriented architecture, secure BCrypt password hashing, role-based access control (RBAC), thread-safe connection pooling, JFreeChart interactive analytics, and high-fidelity PDF/CSV report generation.

---

## 1. System Architecture

The application strictly separates presentation, business logic, persistence, and data models across distinct layers:

```
com.university.grademanagement/
├── Main.java                    # Entry point; configures FlatLaf theme, sets up async pool & launches EDT
├── config/
│   ├── AppConfig.java           # University constants, grading thresholds, and app metadata
│   └── DatabaseConfig.java      # HikariCP connection pool, property persistence & self-initializing schema
├── model/
│   ├── User.java                # Authentication account with role and BCrypt hash
│   ├── Role.java                # Enum: ADMIN, FACULTY, STUDENT
│   ├── Student.java             # Student profile with roll number, department, semester, and personal details
│   ├── Faculty.java             # Faculty profile with employee ID, designation, and qualification
│   ├── Subject.java             # Course details with credits, internal max, external max, and pass marks
│   ├── FacultySubject.java      # Course allocation mapping faculty to subjects
│   ├── AcademicYear.java        # Academic period representation (e.g. 2025-2026)
│   ├── Marks.java               # Component marks (internal + external = total) with grade & points
│   ├── GradeResult.java         # Composite semester evaluation with SGPA, CGPA, and pass/fail standing
│   ├── SubjectResult.java       # Evaluated subject line item on student grade cards
│   ├── StudentRank.java         # Cohort ranking record with percentage, SGPA, and eligibility status
│   ├── AnalyticsSummary.java    # Aggregated metrics for institutional charts and executive dashboard
│   └── AuditLog.java            # Cryptographic activity record with timestamp, user, action, and IP
├── dao/
│   ├── UserDao.java & UserDaoImpl.java
│   ├── StudentDao.java & StudentDaoImpl.java
│   ├── FacultyDao.java & FacultyDaoImpl.java
│   ├── SubjectDao.java & SubjectDaoImpl.java
│   ├── FacultySubjectDao.java & FacultySubjectDaoImpl.java
│   ├── MarksDao.java & MarksDaoImpl.java
│   ├── AcademicYearDao.java & AcademicYearDaoImpl.java
│   └── AuditLogDao.java & AuditLogDaoImpl.java
├── service/
│   ├── AuthService.java         # BCrypt authentication, session context, and role assertion
│   ├── GradeService.java        # Configurable grading scale, 10-point scale grade points, and SGPA rules
│   ├── MarksService.java        # Score recording, batch persistence, and faculty authorization checks
│   ├── StudentService.java      # Student profile CRUD, deactivation, and transactional user provisioning
│   ├── FacultyService.java      # Faculty profile management and teaching assignments
│   ├── SubjectService.java      # Curriculum courses and department allocations
│   ├── RankingService.java      # Cohort merit rankings and at-risk student detection
│   ├── AnalyticsService.java    # Database-backed statistical aggregations for charts
│   └── ReportService.java       # Official PDF marksheets and RFC 4180 CSV exports
├── ui/
│   ├── MainFrame.java           # Primary window with role-tailored dark sidebar and CardLayout
│   ├── LoginFrame.java          # Authentication window with evaluator quick-fill buttons and DB status
│   ├── DbConfigDialog.java      # Interactive MySQL connection configuration and schema seeding dialog
│   ├── common/                  # UITheme, MetricCard, StyledTable, StatusBadgeRenderer, BasePanel
│   ├── admin/                   # AdminDashboardPanel, StudentManagementPanel, SubjectManagementPanel,
│   │                            # FacultyManagementPanel, AnalyticsPanel, AuditLogsPanel
│   ├── faculty/                 # FacultyDashboardPanel, MarksEntryPanel, ClassPerformancePanel
│   └── student/                 # StudentDashboardPanel, PerformanceTrendsPanel
├── util/
│   ├── PasswordUtil.java        # BCrypt salted password hashing and timing-safe verification
│   ├── ValidationUtil.java      # Roll number, email, and marks boundary validation rules
│   ├── CsvExporter.java         # RFC 4180 CSV engine with spreadsheet formula injection mitigation
│   └── PdfReportGenerator.java  # High-resolution PDF grade card & merit ranking generator (OpenPDF)
└── exception/                   # AppException, AuthenticationException, AuthorizationException,
                                 # ValidationException, DatabaseException, DuplicateRecordException
```

---

## 2. Role-Based Permissions Matrix

Security and access controls are enforced at both the **Service Layer** and **Database Access Layer**, not merely by hiding UI buttons:

| Feature / Operation | Administrator | Faculty | Student |
|---|:---:|:---:|:---:|
| **Executive Institutional Dashboard** | Full Access | No | No |
| **Student Directory & Account Creation** | Full CRUD | View Authorized | Profile Only |
| **Faculty & Course Allocation** | Full Access | View Assigned | No |
| **Marks Entry & Score Modification** | Full Access | Assigned Courses Only | View Personal Only |
| **Class-wide Analytics & Grade Distribution** | Full Access | Assigned Courses | No |
| **At-Risk Student Intervention Monitoring** | Full Access | Assigned Courses | No |
| **Cohort Merit Rankings** | Full Access | Full Access | Personal Standing Only |
| **System Audit Trail Logs** | Full Access | No | No |
| **Export Official Marksheet (PDF/CSV)** | All Students | Assigned Courses | Personal Marksheet |
| **Database Configuration & Schema Init** | Full Access | No | No |

---

## 3. Grading Policy & SGPA Calculation Algorithm

The system features a centralized, configurable evaluation engine (`GradeService.java`):

### Letter Grade and Point Conversion Scale
- **90.00% – 100.00%**: `A+` (Grade Point: 10.0)
- **80.00% – 89.99%**: `A` (Grade Point: 9.0)
- **70.00% – 79.99%**: `B` (Grade Point: 8.0)
- **60.00% – 69.99%**: `C` (Grade Point: 7.0)
- **50.00% – 59.99%**: `D` (Grade Point: 6.0)
- **40.00% – 49.99%**: `E` (Grade Point: 5.0)
- **Below 40.00%**: `F` (Grade Point: 0.0 — Fail)

### Academic Rules Enforced:
1. **Subject-Specific Pass Criteria**: A student must secure at least 40% (or subject passing marks) to pass each individual course.
2. **Backlog & Standing Rule**: If a student fails even one mandatory subject in a semester, their overall status is reported as `FAILED (N Backlogs)` rather than PASSED, regardless of whether their average percentage is high.
3. **Incomplete Results Handling**: Missing assessments are explicitly flagged as `INCOMPLETE`, and incomplete students are omitted from finalized merit rank lists.
4. **Credit-Weighted SGPA Formula**:
   $$\text{SGPA} = \frac{\sum (\text{Course Credits} \times \text{Grade Point})}{\sum \text{Course Credits}}$$

---

## 4. Pre-Seeded Evaluation Accounts

The database comes pre-seeded with realistic academic profiles. All passwords are encrypted with salted **BCrypt** hashes:

| Role | Username | Password | Account Holder / Details |
|---|---|---|---|
| **Administrator** | `admin` | `Admin@123` | System Administrator (Full Access) |
| **Faculty** | `dr.sharma` | `Faculty@123` | Dr. Ramesh Sharma (Assigned CS501 OS & CS504 SE) |
| **Faculty** | `prof.patel` | `Faculty@123` | Prof. Priya Patel (Assigned CS502 DBMS) |
| **Faculty** | `dr.verma` | `Faculty@123` | Dr. Alok Verma (Assigned CS503 CN & CS505 AI) |
| **Student** | `cs2023001` | `Student@123` | Aarav Sharma (Rank 1 — 93.6% Distinction) |
| **Student** | `cs2023002` | `Student@123` | Diya Patel (Rank 2 — 87.4% First Class) |
| **Student** | `cs2023005` | `Student@123` | Vikram Singh (At-Risk / 1 Backlog in CS503) |

> **Evaluator Tip**: The Login screen includes one-click **"Quick Demo Profiles"** buttons to automatically fill Admin, Faculty, or Student credentials without manual typing!

---

## 5. Setup & Execution Instructions

### Prerequisites
- **Java Development Kit (JDK)**: Java 17 or higher (Verified compatible with Java 17, 21, and 26).
- **MySQL Server 8.0**: Installed and running locally.

### Step 1: Database Setup
You can set up the MySQL database in any of the following ways:

#### Option A: Automatic Setup from GUI (Recommended)
1. Run the application (see Step 2).
2. On the Login screen, click **"MySQL Settings..."** in the bottom status bar.
3. Verify your MySQL host (`localhost`), port (`3306`), username, and password.
4. Click **"Test Connection"**, then click **"Create DB & Seed Data"**.
5. The system will automatically create the `student_grade_db` database, execute `database/schema.sql`, and populate sample students, courses, and marks from `database/seed_data.sql`!

#### Option B: Manual CLI Setup
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed_data.sql
```

#### Option C: Configuration File (`db.properties`)
Edit `db.properties` in the project root:
```properties
db.host=localhost
db.port=3306
db.name=student_grade_db
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

---

### Step 2: Running the Application

#### Option 1: Double-Click Launcher (Windows)
Double-click `run.bat` in the project directory.

#### Option 2: Running with Maven Wrapper
```bash
.\mvnw.cmd compile exec:java -D"exec.mainClass=com.university.grademanagement.Main"
```

#### Option 3: Running Packaged Fat JAR
```bash
java -jar target/student-performance-system-1.0.0.jar
```

---

## 6. Building and Testing

To compile the codebase, execute JUnit 5 tests, and build the standalone Fat JAR:

```bash
# Run Unit Tests
.\mvnw.cmd test

# Build Standalone Executable JAR
.\mvnw.cmd clean package
```

The resulting all-inclusive JAR is generated at:
`target/student-performance-system-1.0.0.jar`

---

## 7. Security Features

- **BCrypt Password Hashing**: Passwords are never stored in plaintext. They are salted with 10 rounds of BCrypt work factor.
- **SQL Injection Prevention**: 100% of database queries utilize `PreparedStatement` with typed parameters.
- **Spreadsheet Formula Injection Mitigation**: Any exported CSV data cells starting with `=`, `+`, `-`, `@`, tab, or return are automatically escaped with a leading single quote `'` according to OWASP guidelines.
- **Timing & Username Enumeration Protection**: Login authentication utilizes generic error messaging to avoid revealing whether an account username exists.
- **Audit Logging**: Sensitive operations (e.g. logins, marks edits, student registration, report exports) are immutably logged with timestamp, user ID, action, and IP address.
- **Non-destructive Account Deactivation**: When a student or faculty account is deactivated, their login is disabled while preserving complete academic marks and transcript history.

---

## 8. Exported Sample Documents

- **Student Semester Marksheet (PDF)**: Formatted university certificate with student details, course evaluation table, SGPA/CGPA cards, grading legend, and signature blocks.
- **Institutional Merit List (PDF)**: Ranked cohort report highlighting top performers, percentage, SGPA, and academic standing.
- **Marks Sheet & Directories (CSV)**: UTF-8 BOM compliant files readable in Microsoft Excel and Google Sheets without encoding issues.

---

## 9. License & Academic Attribution
Developed for the university 5th Semester Java Project Examination. Academic institutions are free to use and extend this software under the MIT License.
