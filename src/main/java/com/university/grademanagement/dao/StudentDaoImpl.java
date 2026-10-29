package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of StudentDao.
 */
public class StudentDaoImpl implements StudentDao {

    private static final Logger logger = LoggerFactory.getLogger(StudentDaoImpl.class);

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setGender(rs.getString("gender"));
        s.setDateOfBirth(rs.getDate("date_of_birth"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setCurrentSemester(rs.getInt("current_semester"));
        s.setAdmissionYear(rs.getInt("admission_year"));
        s.setAddress(rs.getString("address"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianContact(rs.getString("guardian_contact"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));

        // If username is joined
        try {
            s.setUsername(rs.getString("username"));
        } catch (SQLException ignored) {}

        return s;
    }

    @Override
    public Optional<Student> findById(int id) {
        String sql = "SELECT s.*, u.username FROM students s JOIN users u ON s.user_id = u.id WHERE s.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student by id {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to find student", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByUserId(int userId) {
        String sql = "SELECT s.*, u.username FROM students s JOIN users u ON s.user_id = u.id WHERE s.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student by user_id {}: {}", userId, e.getMessage());
            throw new DatabaseException("Failed to find student by user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        String sql = "SELECT s.*, u.username FROM students s JOIN users u ON s.user_id = u.id WHERE s.roll_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student by roll number {}: {}", rollNumber, e.getMessage());
            throw new DatabaseException("Failed to find student by roll number", e);
        }
        return Optional.empty();
    }

    @Override
    public Student create(Student student, Connection externalConn) {
        String sql = "INSERT INTO students (user_id, roll_number, first_name, last_name, gender, date_of_birth, " +
                "email, phone, department, current_semester, admission_year, address, guardian_name, guardian_contact, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean managedExternally = externalConn != null;
        Connection conn = null;
        try {
            conn = managedExternally ? externalConn : DatabaseConfig.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, student.getUserId());
                ps.setString(2, student.getRollNumber().trim().toUpperCase());
                ps.setString(3, student.getFirstName());
                ps.setString(4, student.getLastName());
                ps.setString(5, student.getGender());
                ps.setDate(6, student.getDateOfBirth());
                ps.setString(7, student.getEmail());
                ps.setString(8, student.getPhone());
                ps.setString(9, student.getDepartment());
                ps.setInt(10, student.getCurrentSemester());
                ps.setInt(11, student.getAdmissionYear());
                ps.setString(12, student.getAddress());
                ps.setString(13, student.getGuardianName());
                ps.setString(14, student.getGuardianContact());
                ps.setBoolean(15, student.isActive());

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new DatabaseException("Creating student failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getInt(1));
                    }
                }
                return student;
            }
        } catch (SQLException e) {
            logger.error("Error creating student {}: {}", student.getRollNumber(), e.getMessage());
            throw new DatabaseException("Failed to create student record: " + e.getMessage(), e);
        } finally {
            if (!managedExternally && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, gender = ?, date_of_birth = ?, " +
                "email = ?, phone = ?, department = ?, current_semester = ?, admission_year = ?, address = ?, " +
                "guardian_name = ?, guardian_contact = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getGender());
            ps.setDate(4, student.getDateOfBirth());
            ps.setString(5, student.getEmail());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getDepartment());
            ps.setInt(8, student.getCurrentSemester());
            ps.setInt(9, student.getAdmissionYear());
            ps.setString(10, student.getAddress());
            ps.setString(11, student.getGuardianName());
            ps.setString(12, student.getGuardianContact());
            ps.setBoolean(13, student.isActive());
            ps.setInt(14, student.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating student {}: {}", student.getId(), e.getMessage());
            throw new DatabaseException("Failed to update student", e);
        }
    }

    @Override
    public boolean setActive(int id, boolean active) {
        String sql = "UPDATE students SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting active status for student {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to update student status", e);
        }
    }

    @Override
    public List<Student> findAll(String searchKeyword, String department, Integer semester, Boolean activeOnly, int offset, int limit) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, u.username FROM students s JOIN users u ON s.user_id = u.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (s.roll_number LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ? OR s.email LIKE ?) ");
            String term = "%" + searchKeyword.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND s.department = ? ");
            params.add(department.trim());
        }

        if (semester != null && semester > 0) {
            sql.append("AND s.current_semester = ? ");
            params.add(semester);
        }

        if (activeOnly != null) {
            sql.append("AND s.is_active = ? ");
            params.add(activeOnly);
        }

        sql.append("ORDER BY s.roll_number ASC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 100);
        params.add(Math.max(offset, 0));

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
            logger.error("Error fetching students: {}", e.getMessage());
            throw new DatabaseException("Failed to query student records", e);
        }
        return list;
    }

    @Override
    public int count(String searchKeyword, String department, Integer semester, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM students s WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (s.roll_number LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ? OR s.email LIKE ?) ");
            String term = "%" + searchKeyword.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            sql.append("AND s.department = ? ");
            params.add(department.trim());
        }

        if (semester != null && semester > 0) {
            sql.append("AND s.current_semester = ? ");
            params.add(semester);
        }

        if (activeOnly != null) {
            sql.append("AND s.is_active = ? ");
            params.add(activeOnly);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting students: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public List<String> getDistinctDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM students WHERE department IS NOT NULL AND department <> '' ORDER BY department ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("department"));
            }
        } catch (SQLException e) {
            logger.error("Error getting distinct departments: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean existsByRollNumber(String rollNumber, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_number = ?" + (excludeId != null ? " AND id <> ?" : "");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking roll number existence: {}", e.getMessage());
        }
        return false;
    }
}
