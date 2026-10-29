package com.university.grademanagement.ui.admin;

import com.university.grademanagement.model.Role;
import com.university.grademanagement.model.Student;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.AuthService;
import com.university.grademanagement.service.StudentService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.StatusBadgeRenderer;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;
import com.university.grademanagement.util.CsvExporter;
import com.university.grademanagement.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Student Directory and Profile Management Panel.
 * Supports search, filtering, adding, editing, and safe deactivation.
 */
public class StudentManagementPanel extends BasePanel {

    private final StudentService studentService;
    private final User currentUser;

    private JTextField txtSearch;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbSemester;
    private JCheckBox chkActiveOnly;

    private StyledTable table;
    private DefaultTableModel tableModel;
    private List<Student> currentStudentList;

    public StudentManagementPanel(User currentUser) {
        super("Student Management", "Register, manage, and monitor enrolled student profiles");
        this.studentService = new StudentService();
        this.currentUser = currentUser;
        this.currentStudentList = new ArrayList<>();

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Action buttons
        JButton btnAdd = UITheme.createPrimaryButton("+ Add Student");
        btnAdd.addActionListener(e -> showStudentDialog(null));

        JButton btnEdit = UITheme.createSecondaryButton("Edit Student");
        btnEdit.addActionListener(e -> editSelectedStudent());

        JButton btnToggle = UITheme.createSecondaryButton("Toggle Status");
        btnToggle.addActionListener(e -> toggleSelectedStudent());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCsv());

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnToggle);
        actionsPanel.add(btnExport);

        // Filter and Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        txtSearch = UITheme.createTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name or roll...");
        txtSearch.addActionListener(e -> refreshData());

        cmbDepartment = new JComboBox<>(new String[]{"All Departments", "Computer Science & Engineering", "Information Technology", "Electronics & Comm."});
        cmbDepartment.setFont(UITheme.FONT_REGULAR);
        cmbDepartment.setBackground(Color.WHITE);
        cmbDepartment.addActionListener(e -> refreshData());

        cmbSemester = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"});
        cmbSemester.setFont(UITheme.FONT_REGULAR);
        cmbSemester.setBackground(Color.WHITE);
        cmbSemester.addActionListener(e -> refreshData());

        chkActiveOnly = new JCheckBox("Active Only", true);
        chkActiveOnly.setFont(UITheme.FONT_REGULAR);
        chkActiveOnly.setOpaque(false);
        chkActiveOnly.addActionListener(e -> refreshData());

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> refreshData());

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        filterBar.add(new JLabel("Dept:"));
        filterBar.add(cmbDepartment);
        filterBar.add(new JLabel("Sem:"));
        filterBar.add(cmbSemester);
        filterBar.add(chkActiveOnly);
        filterBar.add(btnSearch);

        // Table
        String[] columns = {"ID", "Roll Number", "Full Name", "Department", "Semester", "Email", "Phone", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = StyledTable.wrapInScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(filterBar, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading student records...");

        String search = txtSearch.getText().trim();
        String dept = (String) cmbDepartment.getSelectedItem();
        if ("All Departments".equals(dept)) dept = null;

        int semIdx = cmbSemester.getSelectedIndex();
        Integer sem = semIdx > 0 ? semIdx : null;
        Boolean activeOnly = chkActiveOnly.isSelected() ? true : null;

        String finalDept = dept;
        SwingWorker<List<Student>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Student> doInBackground() {
                return studentService.getAllStudents(search, finalDept, sem, activeOnly, 0, 500, currentUser);
            }

            @Override
            protected void done() {
                try {
                    currentStudentList = get();
                    tableModel.setRowCount(0);
                    for (Student s : currentStudentList) {
                        tableModel.addRow(new Object[]{
                                s.getId(),
                                s.getRollNumber(),
                                s.getFullName(),
                                s.getDepartment(),
                                "Sem " + s.getCurrentSemester(),
                                s.getEmail(),
                                s.getPhone() != null ? s.getPhone() : "-",
                                s.isActive() ? "Active" : "Deactivated"
                        });
                    }
                    setStatus("Loaded " + currentStudentList.size() + " students.");
                } catch (Exception e) {
                    setStatus("Error loading students: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void editSelectedStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student student = currentStudentList.get(selectedRow);
        showStudentDialog(student);
    }

    private void toggleSelectedStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student student = currentStudentList.get(selectedRow);
        boolean newStatus = !student.isActive();
        String actionStr = newStatus ? "activate" : "deactivate";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + actionStr + " student " + student.getRollNumber() + " (" + student.getFullName() + ")?\n" +
                        (newStatus ? "Student will regain login access." : "Student account will be disabled, but all academic history will be preserved."),
                "Confirm Account Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.toggleStudentStatus(student.getId(), newStatus, currentUser);
                refreshData();
                JOptionPane.showMessageDialog(this, "Student status updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCsv() {
        if (currentStudentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records available to export.", "Empty Table", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("student_directory.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<String> headers = List.of("Roll Number", "First Name", "Last Name", "Gender", "Email", "Phone", "Department", "Semester", "Admission Year", "Status");
                List<List<String>> rows = new ArrayList<>();
                for (Student s : currentStudentList) {
                    rows.add(List.of(
                            s.getRollNumber(),
                            s.getFirstName(),
                            s.getLastName(),
                            s.getGender(),
                            s.getEmail(),
                            s.getPhone() != null ? s.getPhone() : "",
                            s.getDepartment(),
                            String.valueOf(s.getCurrentSemester()),
                            String.valueOf(s.getAdmissionYear()),
                            s.isActive() ? "Active" : "Deactivated"
                    ));
                }
                CsvExporter.exportToFile(chooser.getSelectedFile(), headers, rows);
                JOptionPane.showMessageDialog(this, "Student directory exported successfully to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStudentDialog(Student existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Student Profile" : "Register New Student", true);
        dlg.setSize(480, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField txtRoll = UITheme.createTextField(15);
        JTextField txtFirst = UITheme.createTextField(15);
        JTextField txtLast = UITheme.createTextField(15);
        JComboBox<String> cmbGender = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        JTextField txtEmail = UITheme.createTextField(15);
        JTextField txtPhone = UITheme.createTextField(15);
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"Computer Science & Engineering", "Information Technology", "Electronics & Comm."});
        JComboBox<Integer> cmbSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        JTextField txtYear = UITheme.createTextField(15);
        txtYear.setText("2023");
        JTextField txtGuardian = UITheme.createTextField(15);
        JTextField txtGuardianPhone = UITheme.createTextField(15);

        if (isEdit) {
            txtRoll.setText(existing.getRollNumber());
            txtRoll.setEnabled(false); // Roll number is immutable key
            txtFirst.setText(existing.getFirstName());
            txtLast.setText(existing.getLastName());
            cmbGender.setSelectedItem(existing.getGender());
            txtEmail.setText(existing.getEmail());
            txtPhone.setText(existing.getPhone());
            cmbDept.setSelectedItem(existing.getDepartment());
            cmbSem.setSelectedItem(existing.getCurrentSemester());
            txtYear.setText(String.valueOf(existing.getAdmissionYear()));
            txtGuardian.setText(existing.getGuardianName());
            txtGuardianPhone.setText(existing.getGuardianContact());
        }

        int row = 0;
        addDialogRow(form, gbc, row++, "Roll Number *:", txtRoll);
        addDialogRow(form, gbc, row++, "First Name *:", txtFirst);
        addDialogRow(form, gbc, row++, "Last Name *:", txtLast);
        addDialogRow(form, gbc, row++, "Gender:", cmbGender);
        addDialogRow(form, gbc, row++, "Email Address *:", txtEmail);
        addDialogRow(form, gbc, row++, "Phone Number:", txtPhone);
        addDialogRow(form, gbc, row++, "Department *:", cmbDept);
        addDialogRow(form, gbc, row++, "Current Semester *:", cmbSem);
        addDialogRow(form, gbc, row++, "Admission Year *:", txtYear);
        addDialogRow(form, gbc, row++, "Guardian Name:", txtGuardian);
        addDialogRow(form, gbc, row++, "Guardian Contact:", txtGuardianPhone);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton(isEdit ? "Update Profile" : "Register Student");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            try {
                if (isEdit) {
                    existing.setFirstName(txtFirst.getText().trim());
                    existing.setLastName(txtLast.getText().trim());
                    existing.setGender((String) cmbGender.getSelectedItem());
                    existing.setEmail(txtEmail.getText().trim());
                    existing.setPhone(txtPhone.getText().trim());
                    existing.setDepartment((String) cmbDept.getSelectedItem());
                    existing.setCurrentSemester((Integer) cmbSem.getSelectedItem());
                    existing.setAdmissionYear(Integer.parseInt(txtYear.getText().trim()));
                    existing.setGuardianName(txtGuardian.getText().trim());
                    existing.setGuardianContact(txtGuardianPhone.getText().trim());

                    studentService.updateStudent(existing, currentUser);
                    JOptionPane.showMessageDialog(dlg, "Student profile updated successfully!");
                } else {
                    Student s = new Student();
                    s.setRollNumber(txtRoll.getText().trim());
                    s.setFirstName(txtFirst.getText().trim());
                    s.setLastName(txtLast.getText().trim());
                    s.setGender((String) cmbGender.getSelectedItem());
                    s.setEmail(txtEmail.getText().trim());
                    s.setPhone(txtPhone.getText().trim());
                    s.setDepartment((String) cmbDept.getSelectedItem());
                    s.setCurrentSemester((Integer) cmbSem.getSelectedItem());
                    s.setAdmissionYear(Integer.parseInt(txtYear.getText().trim()));
                    s.setGuardianName(txtGuardian.getText().trim());
                    s.setGuardianContact(txtGuardianPhone.getText().trim());

                    studentService.createStudent(s, "Student@123", currentUser);
                    JOptionPane.showMessageDialog(dlg, "Student registered successfully!\nDefault login password: Student@123");
                }
                dlg.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Validation / Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnSave);
        footer.add(btnCancel);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addDialogRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
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
