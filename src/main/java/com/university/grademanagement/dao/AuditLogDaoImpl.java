package com.university.grademanagement.dao;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC PreparedStatement implementation of AuditLogDao.
 */
public class AuditLogDaoImpl implements AuditLogDao {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDaoImpl.class);

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId((Integer) rs.getObject("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        return log;
    }

    @Override
    public void log(AuditLog auditLog) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (auditLog.getUserId() != null) {
                ps.setInt(1, auditLog.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, auditLog.getUsername());
            ps.setString(3, auditLog.getAction());
            ps.setString(4, auditLog.getEntityType());
            ps.setString(5, auditLog.getEntityId());
            ps.setString(6, auditLog.getDetails());
            ps.setString(7, auditLog.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Override
    public List<AuditLog> findAll(int offset, int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            ps.setInt(2, Math.max(offset, 0));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error reading audit logs: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<AuditLog> findByUser(int userId, int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error reading audit logs by user: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<AuditLog> findByAction(String action, int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error reading audit logs by action: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting audit logs: {}", e.getMessage());
        }
        return 0;
    }
}
