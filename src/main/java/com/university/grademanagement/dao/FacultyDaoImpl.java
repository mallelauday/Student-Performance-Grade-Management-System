package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.Faculty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of FacultyDao.
 */
public class FacultyDaoImpl implements FacultyDao {

    private static final Logger logger = LoggerFactory.getLogger(FacultyDaoImpl.class);

    private Faculty mapRow(ResultSet rs) throws SQLException {
        Faculty f = new Faculty();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setEmployeeId(rs.getString("employee_id"));
        f.setFirstName(rs.getString("first_name"));
        f.setLastName(rs.getString("last_name"));
        f.setDepartment(rs.getString("department"));
        f.setDesignation(rs.getString("designation"));
        f.setQualification(rs.getString("qualification"));
        f.setEmail(rs.getString("email"));
        f.setPhone(rs.getString("phone"));
        f.setActive(rs.getBoolean("is_active"));
        f.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            f.setUsername(rs.getString("username"));
        } catch (SQLException ignored) {}

        return f;
    }

    @Override
    public Optional<Faculty> findById(int id) {
        String sql = "SELECT f.*, u.username FROM faculties f JOIN users u ON f.user_id = u.id WHERE f.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding faculty by id {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to find faculty", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Faculty> findByUserId(int userId) {
        String sql = "SELECT f.*, u.username FROM faculties f JOIN users u ON f.user_id = u.id WHERE f.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding faculty by user_id {}: {}", userId, e.getMessage());
            throw new DatabaseException("Failed to find faculty by user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Faculty> findByEmployeeId(String employeeId) {
        String sql = "SELECT f.*, u.username FROM faculties f JOIN users u ON f.user_id = u.id WHERE f.employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding faculty by employee_id {}: {}", employeeId, e.getMessage());
            throw new DatabaseException("Failed to find faculty by employee id", e);
        }
        return Optional.empty();
    }

    @Override
    public Faculty create(Faculty faculty, Connection externalConn) {
        String sql = "INSERT INTO faculties (user_id, employee_id, first_name, last_name, department, designation, qualification, email, phone, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean managedExternally = externalConn != null;
        Connection conn = null;
        try {
            conn = managedExternally ? externalConn : DatabaseConfig.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, faculty.getUserId());
                ps.setString(2, faculty.getEmployeeId().trim().toUpperCase());
                ps.setString(3, faculty.getFirstName());
                ps.setString(4, faculty.getLastName());
                ps.setString(5, faculty.getDepartment());
                ps.setString(6, faculty.getDesignation());
                ps.setString(7, faculty.getQualification());
                ps.setString(8, faculty.getEmail());
                ps.setString(9, faculty.getPhone());
                ps.setBoolean(10, faculty.isActive());

                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        faculty.setId(generatedKeys.getInt(1));
                    }
                }
                return faculty;
            }
        } catch (SQLException e) {
            logger.error("Error creating faculty {}: {}", faculty.getEmployeeId(), e.getMessage());
            throw new DatabaseException("Failed to create faculty record", e);
        } finally {
            if (!managedExternally && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean update(Faculty faculty) {
        String sql = "UPDATE faculties SET first_name = ?, last_name = ?, department = ?, designation = ?, " +
                "qualification = ?, email = ?, phone = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faculty.getFirstName());
            ps.setString(2, faculty.getLastName());
            ps.setString(3, faculty.getDepartment());
            ps.setString(4, faculty.getDesignation());
            ps.setString(5, faculty.getQualification());
            ps.setString(6, faculty.getEmail());
            ps.setString(7, faculty.getPhone());
            ps.setBoolean(8, faculty.isActive());
            ps.setInt(9, faculty.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating faculty {}: {}", faculty.getId(), e.getMessage());
            throw new DatabaseException("Failed to update faculty", e);
        }
    }

    @Override
    public boolean setActive(int id, boolean active) {
        String sql = "UPDATE faculties SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting active status for faculty {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to update faculty status", e);
        }
    }

    @Override
    public List<Faculty> findAll(boolean activeOnly) {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT f.*, u.username FROM faculties f JOIN users u ON f.user_id = u.id " +
                (activeOnly ? "WHERE f.is_active = TRUE " : "") +
                "ORDER BY f.first_name ASC, f.last_name ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching faculties: {}", e.getMessage());
            throw new DatabaseException("Failed to list faculties", e);
        }
        return list;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM faculties WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting faculties: {}", e.getMessage());
        }
        return 0;
    }
}
