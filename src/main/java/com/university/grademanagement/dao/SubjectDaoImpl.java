package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of SubjectDao.
 */
public class SubjectDaoImpl implements SubjectDao {

    private static final Logger logger = LoggerFactory.getLogger(SubjectDaoImpl.class);

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setDepartment(rs.getString("department"));
        s.setSemester(rs.getInt("semester"));
        s.setCredits(rs.getInt("credits"));
        s.setMaxInternalMarks(rs.getDouble("max_internal_marks"));
        s.setMaxExternalMarks(rs.getDouble("max_external_marks"));
        s.setMaxMarks(rs.getDouble("max_marks"));
        s.setPassMarks(rs.getDouble("pass_marks"));
        s.setElective(rs.getBoolean("is_elective"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }

    @Override
    public Optional<Subject> findById(int id) {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding subject by id {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to find subject", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        String sql = "SELECT * FROM subjects WHERE subject_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding subject by code {}: {}", code, e.getMessage());
            throw new DatabaseException("Failed to find subject by code", e);
        }
        return Optional.empty();
    }

    @Override
    public Subject create(Subject subject) {
        String sql = "INSERT INTO subjects (subject_code, subject_name, department, semester, credits, " +
                "max_internal_marks, max_external_marks, max_marks, pass_marks, is_elective, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getSubjectCode().trim().toUpperCase());
            ps.setString(2, subject.getSubjectName());
            ps.setString(3, subject.getDepartment());
            ps.setInt(4, subject.getSemester());
            ps.setInt(5, subject.getCredits());
            ps.setDouble(6, subject.getMaxInternalMarks());
            ps.setDouble(7, subject.getMaxExternalMarks());
            ps.setDouble(8, subject.getMaxMarks());
            ps.setDouble(9, subject.getPassMarks());
            ps.setBoolean(10, subject.isElective());
            ps.setBoolean(11, subject.isActive());

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subject.setId(generatedKeys.getInt(1));
                }
            }
            return subject;
        } catch (SQLException e) {
            logger.error("Error creating subject {}: {}", subject.getSubjectCode(), e.getMessage());
            throw new DatabaseException("Failed to create subject", e);
        }
    }

    @Override
    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET subject_name = ?, department = ?, semester = ?, credits = ?, " +
                "max_internal_marks = ?, max_external_marks = ?, max_marks = ?, pass_marks = ?, is_elective = ?, is_active = ? " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getDepartment());
            ps.setInt(3, subject.getSemester());
            ps.setInt(4, subject.getCredits());
            ps.setDouble(5, subject.getMaxInternalMarks());
            ps.setDouble(6, subject.getMaxExternalMarks());
            ps.setDouble(7, subject.getMaxMarks());
            ps.setDouble(8, subject.getPassMarks());
            ps.setBoolean(9, subject.isElective());
            ps.setBoolean(10, subject.isActive());
            ps.setInt(11, subject.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating subject {}: {}", subject.getId(), e.getMessage());
            throw new DatabaseException("Failed to update subject", e);
        }
    }

    @Override
    public boolean setActive(int id, boolean active) {
        String sql = "UPDATE subjects SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting active status for subject {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to update subject status", e);
        }
    }

    @Override
    public List<Subject> findAll(String department, Integer semester, Boolean activeOnly) {
        List<Subject> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM subjects WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND department = ? ");
            params.add(department.trim());
        }

        if (semester != null && semester > 0) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }

        if (activeOnly != null) {
            sql.append("AND is_active = ? ");
            params.add(activeOnly);
        }

        sql.append("ORDER BY semester ASC, subject_code ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error listing subjects: {}", e.getMessage());
            throw new DatabaseException("Failed to list subjects", e);
        }
        return list;
    }

    @Override
    public List<Subject> findByDepartmentAndSemester(String department, int semester) {
        return findAll(department, semester, true);
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM subjects WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting subjects: {}", e.getMessage());
        }
        return 0;
    }
}
