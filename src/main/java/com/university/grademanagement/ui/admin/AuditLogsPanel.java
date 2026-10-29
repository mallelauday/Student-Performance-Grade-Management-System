package com.university.grademanagement.ui.admin;

import com.university.grademanagement.dao.AuditLogDao;
import com.university.grademanagement.dao.AuditLogDaoImpl;
import com.university.grademanagement.model.AuditLog;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;
import com.university.grademanagement.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * System Audit Trail and Activity Log Monitoring Panel.
 */
public class AuditLogsPanel extends BasePanel {

    private final AuditLogDao auditLogDao;
    private StyledTable table;
    private DefaultTableModel tableModel;

    public AuditLogsPanel() {
        super("System Audit Trail", "Cryptographically verifiable activity logs, authentication events, and marks modifications");
        this.auditLogDao = new AuditLogDaoImpl();

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Logs");
        btnRefresh.addActionListener(e -> refreshData());
        actionsPanel.add(btnRefresh);

        String[] columns = {"ID", "Timestamp", "User Account", "Action Type", "Entity", "Details & Changes", "IP Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(320);

        contentPanel.add(StyledTable.wrapInScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading audit logs...");
        SwingWorker<List<AuditLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuditLog> doInBackground() {
                return auditLogDao.findAll(0, 200);
            }

            @Override
            protected void done() {
                try {
                    List<AuditLog> list = get();
                    tableModel.setRowCount(0);
                    for (AuditLog l : list) {
                        tableModel.addRow(new Object[]{
                                l.getId(),
                                DateUtil.formatTimestamp(l.getTimestamp()),
                                l.getUsername() != null ? l.getUsername() : "system",
                                l.getAction(),
                                l.getEntityType() + (l.getEntityId() != null ? " (" + l.getEntityId() + ")" : ""),
                                l.getDetails(),
                                l.getIpAddress()
                        });
                    }
                    setStatus("Displaying latest " + list.size() + " audit events.");
                } catch (Exception ex) {
                    setStatus("Error loading audit logs: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
