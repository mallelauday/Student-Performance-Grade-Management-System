package com.university.grademanagement.ui.admin;

import com.university.grademanagement.model.Faculty;
import com.university.grademanagement.model.FacultySubject;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.FacultyService;
import com.university.grademanagement.service.SubjectService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.StatusBadgeRenderer;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Faculty Staff Directory and Assignment Management Panel.
 */
public class FacultyManagementPanel extends BasePanel {

    private final FacultyService facultyService;
    private final SubjectService subjectService;
    private final User currentUser;

    private StyledTable table;
    private DefaultTableModel tableModel;
    private List<Faculty> facultyList;

    public FacultyManagementPanel(User currentUser) {
        super("Faculty Directory", "Manage teaching staff accounts, department designations, and course assignments");
        this.facultyService = new FacultyService();
        this.subjectService = new SubjectService();
        this.currentUser = currentUser;
        this.facultyList = new ArrayList<>();

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnAdd = UITheme.createPrimaryButton("+ Add Faculty");
        btnAdd.addActionListener(e -> showFacultyDialog(null));

        JButton btnEdit = UITheme.createSecondaryButton("Edit Faculty");
        btnEdit.addActionListener(e -> editSelectedFaculty());

        JButton btnToggle = UITheme.createSecondaryButton("Toggle Status");
        btnToggle.addActionListener(e -> toggleSelectedFaculty());

        JButton btnCourses = UITheme.createSecondaryButton("Assigned Courses");
        btnCourses.addActionListener(e -> viewAssignedCourses());

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnToggle);
        actionsPanel.add(btnCourses);

        String[] columns = {"ID", "Employee ID", "Full Name", "Department", "Designation", "Qualification", "Email", "Phone", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setMaxWidth(110);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        contentPanel.add(StyledTable.wrapInScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading faculty directory...");
        SwingWorker<List<Faculty>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Faculty> doInBackground() {
                return facultyService.getAllFaculty(false);
            }

            @Override
            protected void done() {
                try {
                    facultyList = get();
                    tableModel.setRowCount(0);
                    for (Faculty f : facultyList) {
                        tableModel.addRow(new Object[]{
                                f.getId(),
                                f.getEmployeeId(),
                                f.getFullName(),
                                f.getDepartment(),
                                f.getDesignation(),
                                f.getQualification() != null ? f.getQualification() : "-",
                                f.getEmail(),
                                f.getPhone() != null ? f.getPhone() : "-",
                                f.isActive() ? "Active" : "Deactivated"
                        });
                    }
                    setStatus("Loaded " + facultyList.size() + " faculty members.");
                } catch (Exception e) {
                    setStatus("Error loading faculty: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void editSelectedFaculty() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to edit.", "Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Faculty f = facultyList.get(row);
        showFacultyDialog(f);
    }

    private void toggleSelectedFaculty() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member.", "Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Faculty f = facultyList.get(row);
        boolean newStatus = !f.isActive();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Change status for " + f.getFullName() + " to " + (newStatus ? "Active" : "Deactivated") + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            facultyService.toggleFacultyStatus(f.getId(), newStatus, currentUser);
            refreshData();
        }
    }

    private void viewAssignedCourses() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member.", "Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Faculty f = facultyList.get(row);
        List<FacultySubject> assignments = subjectService.getFacultyAssignments(f.getId(), null);

        StringBuilder sb = new StringBuilder();
        sb.append("Assigned Courses for ").append(f.getFullName()).append(":\n\n");
        if (assignments.isEmpty()) {
            sb.append("No active course assignments found.");
        } else {
            for (FacultySubject fs : assignments) {
                sb.append("• ").append(fs.getSubjectCode()).append(" - ").append(fs.getSubjectName())
                        .append(" (Semester ").append(fs.getSemester()).append(", ").append(fs.getAcademicYearCode()).append(")\n");
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Faculty Assignments", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showFacultyDialog(Faculty existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Faculty Profile" : "Add Faculty Member", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtEmpId = UITheme.createTextField(15);
        JTextField txtFirst = UITheme.createTextField(15);
        JTextField txtLast = UITheme.createTextField(15);
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"Computer Science & Engineering", "Information Technology", "Electronics & Comm."});
        JComboBox<String> cmbDesig = new JComboBox<>(new String[]{"Professor & HOD", "Professor", "Associate Professor", "Assistant Professor", "Visiting Lecturer"});
        JTextField txtQual = UITheme.createTextField(15);
        txtQual.setText("Ph.D. in Computer Science");
        JTextField txtEmail = UITheme.createTextField(15);
        JTextField txtPhone = UITheme.createTextField(15);

        if (isEdit) {
            txtEmpId.setText(existing.getEmployeeId());
            txtEmpId.setEnabled(false);
            txtFirst.setText(existing.getFirstName());
            txtLast.setText(existing.getLastName());
            cmbDept.setSelectedItem(existing.getDepartment());
            cmbDesig.setSelectedItem(existing.getDesignation());
            txtQual.setText(existing.getQualification());
            txtEmail.setText(existing.getEmail());
            txtPhone.setText(existing.getPhone());
        }

        int r = 0;
        addFormRow(form, gbc, r++, "Employee ID *:", txtEmpId);
        addFormRow(form, gbc, r++, "First Name *:", txtFirst);
        addFormRow(form, gbc, r++, "Last Name *:", txtLast);
        addFormRow(form, gbc, r++, "Department *:", cmbDept);
        addFormRow(form, gbc, r++, "Designation *:", cmbDesig);
        addFormRow(form, gbc, r++, "Qualification:", txtQual);
        addFormRow(form, gbc, r++, "Email *:", txtEmail);
        addFormRow(form, gbc, r++, "Phone:", txtPhone);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton(isEdit ? "Update Profile" : "Register Faculty");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            try {
                if (isEdit) {
                    existing.setFirstName(txtFirst.getText().trim());
                    existing.setLastName(txtLast.getText().trim());
                    existing.setDepartment((String) cmbDept.getSelectedItem());
                    existing.setDesignation((String) cmbDesig.getSelectedItem());
                    existing.setQualification(txtQual.getText().trim());
                    existing.setEmail(txtEmail.getText().trim());
                    existing.setPhone(txtPhone.getText().trim());

                    facultyService.updateFaculty(existing, currentUser);
                    JOptionPane.showMessageDialog(dlg, "Faculty profile updated successfully!");
                } else {
                    Faculty f = new Faculty();
                    f.setEmployeeId(txtEmpId.getText().trim());
                    f.setFirstName(txtFirst.getText().trim());
                    f.setLastName(txtLast.getText().trim());
                    f.setDepartment((String) cmbDept.getSelectedItem());
                    f.setDesignation((String) cmbDesig.getSelectedItem());
                    f.setQualification(txtQual.getText().trim());
                    f.setEmail(txtEmail.getText().trim());
                    f.setPhone(txtPhone.getText().trim());

                    facultyService.createFaculty(f, "Faculty@123", currentUser);
                    JOptionPane.showMessageDialog(dlg, "Faculty registered successfully!\nDefault login: " + f.getEmployeeId().toLowerCase() + " / Faculty@123");
                }
                dlg.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnSave);
        footer.add(btnCancel);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }
}
