package com.university.grademanagement.dao;

import com.university.grademanagement.model.AuditLog;

import java.util.List;

/**
 * Data Access Object interface for system audit trail logging.
 */
public interface AuditLogDao {
    void log(AuditLog auditLog);
    List<AuditLog> findAll(int offset, int limit);
    List<AuditLog> findByUser(int userId, int limit);
    List<AuditLog> findByAction(String action, int limit);
    int count();
}
