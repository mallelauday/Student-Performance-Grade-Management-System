package com.university.grademanagement.ui.admin;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.*;
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
 * Curriculum and Subject Management Panel.
 * Supports course creation, maximum/passing marks configuration, and faculty assignment.
 */
public class SubjectManagementPanel extends BasePanel {

    private final SubjectService subjectService;
    private final FacultyService facultyService;
    private final AcademicYearDao academicYearDao;
    private final User currentUser;

    private StyledTable table;
    private DefaultTableModel tableModel;
    private List<Subject> subjectList;
    private JComboBox<String> cmbDeptFilter;
    private JComboBox<String> cmbSemFilter;

    public SubjectManagementPanel(User currentUser) {
        super("Subject & Curriculum Management", "Define courses, evaluation schemes, and assign faculty instructors");
        this.subjectService = new SubjectService();
        this.facultyService = new FacultyService();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.currentUser = currentUser;
        this.subjectList = new ArrayList<>();

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnAdd = UITheme.createPrimaryButton("+ Add Course");
        btnAdd.addActionListener(e -> showSubjectDialog(null));

        JButton btnEdit = UITheme.createSecondaryButton("Edit Course");
        btnEdit.addActionListener(e -> editSelectedSubject());

        JButton btnAssign = UITheme.createSecondaryButton("Assign Faculty...");
        btnAssign.addActionListener(e -> showAssignFacultyDialog());

        JButton btnViewAssignments = UITheme.createSecondaryButton("View All Assignments");
        btnViewAssignments.addActionListener(e -> showAllAssignmentsDialog());

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnAssign);
        actionsPanel.add(btnViewAssignments);

        // Filter toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        cmbDeptFilter = new JComboBox<>(new String[]{"All Departments", "Computer Science & Engineering", "Information Technology", "Electronics & Comm."});
        cmbDeptFilter.setFont(UITheme.FONT_REGULAR);
        cmbDeptFilter.setBackground(Color.WHITE);
        cmbDeptFilter.addActionListener(e -> refreshData());

        cmbSemFilter = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"});
        cmbSemFilter.setFont(UITheme.FONT_REGULAR);
        cmbSemFilter.setBackground(Color.WHITE);
        cmbSemFilter.addActionListener(e -> refreshData());

        filterBar.add(new JLabel("Department:"));
        filterBar.add(cmbDeptFilter);
        filterBar.add(new JLabel("Semester:"));
        filterBar.add(cmbSemFilter);

        // Table
        String[] columns = {"ID", "Code", "Course Name", "Department", "Sem", "Credits", "Internal Max", "External Max", "Total Max", "Pass Marks", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setMaxWidth(60);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(10).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = StyledTable.wrapInScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(filterBar, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading curriculum courses...");
        String dept = (String) cmbDeptFilter.getSelectedItem();
        if ("All Departments".equals(dept)) dept = null;
        int semIdx = cmbSemFilter.getSelectedIndex();
        Integer sem = semIdx > 0 ? semIdx : null;

        String finalDept = dept;
        SwingWorker<List<Subject>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Subject> doInBackground() {
                return subjectService.getAllSubjects(finalDept, sem, true);
            }

            @Override
            protected void done() {
                try {
                    subjectList = get();
                    tableModel.setRowCount(0);
                    for (Subject s : subjectList) {
                        tableModel.addRow(new Object[]{
                                s.getId(),
                                s.getSubjectCode(),
                                s.getSubjectName(),
                                s.getDepartment(),
                                "Sem " + s.getSemester(),
                                s.getCredits(),
                                String.format("%.1f", s.getMaxInternalMarks()),
                                String.format("%.1f", s.getMaxExternalMarks()),
                                String.format("%.1f", s.getMaxMarks()),
                                String.format("%.1f", s.getPassMarks()),
                                s.isActive() ? "Active" : "Inactive"
                        });
                    }
                    setStatus("Loaded " + subjectList.size() + " course records.");
                } catch (Exception e) {
                    setStatus("Error loading subjects: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void editSelectedSubject() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Subject s = subjectList.get(selectedRow);
        showSubjectDialog(s);
    }

    private void showSubjectDialog(Subject existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Course" : "Add New Course", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtCode = UITheme.createTextField(15);
        JTextField txtName = UITheme.createTextField(15);
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"Computer Science & Engineering", "Information Technology", "Electronics & Comm."});
        JComboBox<Integer> cmbSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        JComboBox<Integer> cmbCredits = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
        JTextField txtInternal = UITheme.createTextField(10);
        txtInternal.setText("40.0");
        JTextField txtExternal = UITheme.createTextField(10);
        txtExternal.setText("60.0");
        JTextField txtPass = UITheme.createTextField(10);
        txtPass.setText("40.0");

        if (isEdit) {
            txtCode.setText(existing.getSubjectCode());
            txtCode.setEnabled(false);
            txtName.setText(existing.getSubjectName());
            cmbDept.setSelectedItem(existing.getDepartment());
            cmbSem.setSelectedItem(existing.getSemester());
            cmbCredits.setSelectedItem(existing.getCredits());
            txtInternal.setText(String.valueOf(existing.getMaxInternalMarks()));
            txtExternal.setText(String.valueOf(existing.getMaxExternalMarks()));
            txtPass.setText(String.valueOf(existing.getPassMarks()));
        }

        int r = 0;
        addFormRow(form, gbc, r++, "Course Code *:", txtCode);
        addFormRow(form, gbc, r++, "Course Name *:", txtName);
        addFormRow(form, gbc, r++, "Department *:", cmbDept);
        addFormRow(form, gbc, r++, "Semester *:", cmbSem);
        addFormRow(form, gbc, r++, "Credits *:", cmbCredits);
        addFormRow(form, gbc, r++, "Max Internal Marks *:", txtInternal);
        addFormRow(form, gbc, r++, "Max External Marks *:", txtExternal);
        addFormRow(form, gbc, r++, "Passing Marks *:", txtPass);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton(isEdit ? "Update Course" : "Save Course");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            try {
                double internal = Double.parseDouble(txtInternal.getText().trim());
                double external = Double.parseDouble(txtExternal.getText().trim());
                double pass = Double.parseDouble(txtPass.getText().trim());

                if (isEdit) {
                    existing.setSubjectName(txtName.getText().trim());
                    existing.setDepartment((String) cmbDept.getSelectedItem());
                    existing.setSemester((Integer) cmbSem.getSelectedItem());
                    existing.setCredits((Integer) cmbCredits.getSelectedItem());
                    existing.setMaxInternalMarks(internal);
                    existing.setMaxExternalMarks(external);
                    existing.setPassMarks(pass);

                    subjectService.updateSubject(existing, currentUser);
                    JOptionPane.showMessageDialog(dlg, "Course updated successfully!");
                } else {
                    Subject s = new Subject();
                    s.setSubjectCode(txtCode.getText().trim());
                    s.setSubjectName(txtName.getText().trim());
                    s.setDepartment((String) cmbDept.getSelectedItem());
                    s.setSemester((Integer) cmbSem.getSelectedItem());
                    s.setCredits((Integer) cmbCredits.getSelectedItem());
                    s.setMaxInternalMarks(internal);
                    s.setMaxExternalMarks(external);
                    s.setPassMarks(pass);

                    subjectService.createSubject(s, currentUser);
                    JOptionPane.showMessageDialog(dlg, "Course created successfully!");
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

    private void showAssignFacultyDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Faculty Instructor to Course", true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JComboBox<Subject> cmbSubject = new JComboBox<>();
        subjectList.forEach(cmbSubject::addItem);

        JComboBox<Faculty> cmbFaculty = new JComboBox<>();
        List<Faculty> faculties = facultyService.getAllFaculty(true);
        faculties.forEach(cmbFaculty::addItem);

        JComboBox<AcademicYear> cmbYear = new JComboBox<>();
        List<AcademicYear> years = academicYearDao.findAll();
        years.forEach(cmbYear::addItem);

        JComboBox<Integer> cmbSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cmbSem.setSelectedItem(5);

        int r = 0;
        addFormRow(form, gbc, r++, "Course *:", cmbSubject);
        addFormRow(form, gbc, r++, "Faculty Member *:", cmbFaculty);
        addFormRow(form, gbc, r++, "Academic Period *:", cmbYear);
        addFormRow(form, gbc, r++, "Semester *:", cmbSem);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Save Assignment");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            Subject s = (Subject) cmbSubject.getSelectedItem();
            Faculty f = (Faculty) cmbFaculty.getSelectedItem();
            AcademicYear y = (AcademicYear) cmbYear.getSelectedItem();
            Integer sem = (Integer) cmbSem.getSelectedItem();

            if (s == null || f == null || y == null) {
                JOptionPane.showMessageDialog(dlg, "Please fill all required selections.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                subjectService.assignFaculty(f.getId(), s.getId(), y.getId(), sem, currentUser);
                JOptionPane.showMessageDialog(dlg, "Faculty " + f.getFullName() + " successfully assigned to " + s.getSubjectCode() + "!");
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Assignment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnSave);
        footer.add(btnCancel);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAllAssignmentsDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Current Faculty Subject Assignments", true);
        dlg.setSize(680, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        String[] cols = {"ID", "Course Code", "Course Name", "Faculty Name", "Department", "Academic Period"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        StyledTable tbl = new StyledTable(model);
        tbl.getColumnModel().getColumn(0).setMaxWidth(45);

        List<FacultySubject> assignments = subjectService.getAllAssignments(null);
        for (FacultySubject fs : assignments) {
            model.addRow(new Object[]{
                    fs.getId(),
                    fs.getSubjectCode(),
                    fs.getSubjectName(),
                    fs.getFacultyName(),
                    fs.getFacultyDepartment(),
                    fs.getAcademicYearCode()
            });
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRemove = UITheme.createDangerButton("Remove Assignment");
        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        btnRemove.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select an assignment to remove.", "Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int assignmentId = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(dlg, "Remove this faculty assignment?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                subjectService.unassignFaculty(assignmentId, currentUser);
                model.removeRow(row);
                JOptionPane.showMessageDialog(dlg, "Assignment removed.");
            }
        });

        footer.add(btnRemove);
        footer.add(btnClose);

        dlg.add(StyledTable.wrapInScrollPane(tbl), BorderLayout.CENTER);
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
