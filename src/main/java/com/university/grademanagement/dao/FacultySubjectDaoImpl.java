package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.FacultySubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of FacultySubjectDao.
 */
public class FacultySubjectDaoImpl implements FacultySubjectDao {

    private static final Logger logger = LoggerFactory.getLogger(FacultySubjectDaoImpl.class);

    private FacultySubject mapRow(ResultSet rs) throws SQLException {
        FacultySubject fs = new FacultySubject();
        fs.setId(rs.getInt("id"));
        fs.setFacultyId(rs.getInt("faculty_id"));
        fs.setSubjectId(rs.getInt("subject_id"));
        fs.setAcademicYearId(rs.getInt("academic_year_id"));
        fs.setSemester(rs.getInt("semester"));
        fs.setAssignedDate(rs.getTimestamp("assigned_date"));

        try {
            fs.setFacultyEmployeeId(rs.getString("employee_id"));
            fs.setFacultyName(rs.getString("faculty_name"));
            fs.setFacultyDepartment(rs.getString("faculty_dept"));
            fs.setSubjectCode(rs.getString("subject_code"));
            fs.setSubjectName(rs.getString("subject_name"));
            fs.setAcademicYearCode(rs.getString("year_code"));
        } catch (SQLException ignored) {}

        return fs;
    }

    @Override
    public FacultySubject assign(int facultyId, int subjectId, int academicYearId, int semester) {
        String sql = "INSERT INTO faculty_subjects (faculty_id, subject_id, academic_year_id, semester) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE semester = VALUES(semester)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facultyId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYearId);
            ps.setInt(4, semester);
            ps.executeUpdate();

            FacultySubject fs = new FacultySubject();
            fs.setFacultyId(facultyId);
            fs.setSubjectId(subjectId);
            fs.setAcademicYearId(academicYearId);
            fs.setSemester(semester);

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fs.setId(generatedKeys.getInt(1));
                }
            }
            return fs;
        } catch (SQLException e) {
            logger.error("Error assigning faculty to subject: {}", e.getMessage());
            throw new DatabaseException("Failed to assign faculty to subject", e);
        }
    }

    @Override
    public boolean unassign(int assignmentId) {
        String sql = "DELETE FROM faculty_subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting faculty assignment {}: {}", assignmentId, e.getMessage());
            throw new DatabaseException("Failed to remove faculty assignment", e);
        }
    }

    @Override
    public Optional<FacultySubject> findById(int id) {
        String sql = "SELECT fs.*, f.employee_id, CONCAT(f.first_name, ' ', f.last_name) as faculty_name, f.department as faculty_dept, " +
                "s.subject_code, s.subject_name, y.year_code " +
                "FROM faculty_subjects fs " +
                "JOIN faculties f ON fs.faculty_id = f.id " +
                "JOIN subjects s ON fs.subject_id = s.id " +
                "JOIN academic_years y ON fs.academic_year_id = y.id " +
                "WHERE fs.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding faculty assignment {}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<FacultySubject> findByFaculty(int facultyId, Integer academicYearId) {
        List<FacultySubject> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT fs.*, f.employee_id, CONCAT(f.first_name, ' ', f.last_name) as faculty_name, f.department as faculty_dept, " +
                "s.subject_code, s.subject_name, y.year_code " +
                "FROM faculty_subjects fs " +
                "JOIN faculties f ON fs.faculty_id = f.id " +
                "JOIN subjects s ON fs.subject_id = s.id " +
                "JOIN academic_years y ON fs.academic_year_id = y.id " +
                "WHERE fs.faculty_id = ? ");
        if (academicYearId != null) {
            sql.append("AND fs.academic_year_id = ? ");
        }
        sql.append("ORDER BY s.semester ASC, s.subject_code ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, facultyId);
            if (academicYearId != null) {
                ps.setInt(2, academicYearId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching assignments for faculty {}: {}", facultyId, e.getMessage());
            throw new DatabaseException("Failed to list faculty assignments", e);
        }
        return list;
    }

    @Override
    public List<FacultySubject> findBySubject(int subjectId, Integer academicYearId) {
        List<FacultySubject> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT fs.*, f.employee_id, CONCAT(f.first_name, ' ', f.last_name) as faculty_name, f.department as faculty_dept, " +
                "s.subject_code, s.subject_name, y.year_code " +
                "FROM faculty_subjects fs " +
                "JOIN faculties f ON fs.faculty_id = f.id " +
                "JOIN subjects s ON fs.subject_id = s.id " +
                "JOIN academic_years y ON fs.academic_year_id = y.id " +
                "WHERE fs.subject_id = ? ");
        if (academicYearId != null) {
            sql.append("AND fs.academic_year_id = ? ");
        }
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, subjectId);
            if (academicYearId != null) {
                ps.setInt(2, academicYearId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching assignments for subject {}: {}", subjectId, e.getMessage());
        }
        return list;
    }

    @Override
    public List<FacultySubject> findAll(Integer academicYearId) {
        List<FacultySubject> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT fs.*, f.employee_id, CONCAT(f.first_name, ' ', f.last_name) as faculty_name, f.department as faculty_dept, " +
                "s.subject_code, s.subject_name, y.year_code " +
                "FROM faculty_subjects fs " +
                "JOIN faculties f ON fs.faculty_id = f.id " +
                "JOIN subjects s ON fs.subject_id = s.id " +
                "JOIN academic_years y ON fs.academic_year_id = y.id ");
        if (academicYearId != null) {
            sql.append("WHERE fs.academic_year_id = ? ");
        }
        sql.append("ORDER BY y.year_code DESC, s.semester ASC, s.subject_code ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (academicYearId != null) {
                ps.setInt(1, academicYearId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error listing all faculty assignments: {}", e.getMessage());
            throw new DatabaseException("Failed to list assignments", e);
        }
        return list;
    }

    @Override
    public boolean isFacultyAssignedToSubject(int facultyId, int subjectId, int academicYearId) {
        String sql = "SELECT COUNT(*) FROM faculty_subjects WHERE faculty_id = ? AND subject_id = ? AND academic_year_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYearId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking faculty assignment authorization: {}", e.getMessage());
        }
        return false;
    }
}
