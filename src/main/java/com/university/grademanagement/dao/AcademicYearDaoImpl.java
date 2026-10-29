package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.exception.DatabaseException;
import com.university.grademanagement.model.AcademicYear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of AcademicYearDao.
 */
public class AcademicYearDaoImpl implements AcademicYearDao {

    private static final Logger logger = LoggerFactory.getLogger(AcademicYearDaoImpl.class);

    private AcademicYear mapRow(ResultSet rs) throws SQLException {
        AcademicYear y = new AcademicYear();
        y.setId(rs.getInt("id"));
        y.setYearCode(rs.getString("year_code"));
        y.setYearName(rs.getString("year_name"));
        y.setCurrent(rs.getBoolean("is_current"));
        y.setCreatedAt(rs.getTimestamp("created_at"));
        return y;
    }

    @Override
    public Optional<AcademicYear> findById(int id) {
        String sql = "SELECT * FROM academic_years WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding academic year {}: {}", id, e.getMessage());
            throw new DatabaseException("Failed to find academic year", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<AcademicYear> getCurrentYear() {
        String sql = "SELECT * FROM academic_years WHERE is_current = TRUE LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error finding current academic year: {}", e.getMessage());
            throw new DatabaseException("Failed to find current academic year", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AcademicYear> findAll() {
        List<AcademicYear> list = new ArrayList<>();
        String sql = "SELECT * FROM academic_years ORDER BY year_code DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error listing academic years: {}", e.getMessage());
            throw new DatabaseException("Failed to list academic years", e);
        }
        return list;
    }

    @Override
    public AcademicYear create(AcademicYear year) {
        String sql = "INSERT INTO academic_years (year_code, year_name, is_current) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, year.getYearCode().trim());
            ps.setString(2, year.getYearName().trim());
            ps.setBoolean(3, year.isCurrent());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    year.setId(generatedKeys.getInt(1));
                }
            }
            return year;
        } catch (SQLException e) {
            logger.error("Error creating academic year {}: {}", year.getYearCode(), e.getMessage());
            throw new DatabaseException("Failed to create academic year", e);
        }
    }

    @Override
    public boolean setCurrentYear(int id) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement s1 = conn.createStatement();
                 PreparedStatement s2 = conn.prepareStatement("UPDATE academic_years SET is_current = TRUE WHERE id = ?")) {
                s1.executeUpdate("UPDATE academic_years SET is_current = FALSE");
                s2.setInt(1, id);
                s2.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error setting current academic year: {}", e.getMessage());
            throw new DatabaseException("Failed to update current academic year", e);
        }
    }
}
