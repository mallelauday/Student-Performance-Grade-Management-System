package com.university.grademanagement.ui.faculty;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.*;
import com.university.grademanagement.service.*;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.StatusBadgeRenderer;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;
import com.university.grademanagement.util.CsvExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast, Interactive Marks Entry and Evaluation Workspace.
 * Automatically validates marks in real-time, calculates grades and SGPA points,
 * and securely saves batch records to MySQL.
 */
public class MarksEntryPanel extends BasePanel {

    private final MarksService marksService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final AcademicYearDao academicYearDao;
    private final GradeService gradeService;
    private final User currentUser;

    private JComboBox<SubjectComboItem> cmbSubject;
    private JComboBox<AcademicYear> cmbYear;
    private JLabel lblLimitsInfo;

    private StyledTable table;
    private DefaultTableModel tableModel;
    private boolean isUpdatingTable = false;

    private static class SubjectComboItem {
        final Subject subject;
        SubjectComboItem(Subject s) { this.subject = s; }
        @Override
        public String toString() {
            return subject.getSubjectCode() + " - " + subject.getSubjectName() + " (Sem " + subject.getSemester() + ")";
        }
    }

    public MarksEntryPanel(User currentUser) {
        super("Marks Entry & Evaluation", "Record internal and external examination scores with real-time grade calculation");
        this.marksService = new MarksService();
        this.subjectService = new SubjectService();
        this.studentService = new StudentService();
        this.facultyService = new FacultyService();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.gradeService = new GradeService();
        this.currentUser = currentUser;

        initUI();
        loadCourses();
    }

    private void initUI() {
        JButton btnSave = UITheme.createPrimaryButton("Save All Marks");
        btnSave.addActionListener(e -> saveMarksToDatabase());

        JButton btnExport = UITheme.createSecondaryButton("Export Marks (CSV)");
        btnExport.addActionListener(e -> exportMarksCsv());

        JButton btnReload = UITheme.createSecondaryButton("Reload Roster");
        btnReload.addActionListener(e -> refreshData());

        actionsPanel.add(btnSave);
        actionsPanel.add(btnExport);
        actionsPanel.add(btnReload);

        JPanel mainLayout = new JPanel(new BorderLayout());
        mainLayout.setOpaque(false);

        // Selection Toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        cmbSubject = new JComboBox<>();
        cmbSubject.setFont(UITheme.FONT_REGULAR);
        cmbSubject.setBackground(Color.WHITE);
        cmbSubject.addActionListener(e -> {
            updateLimitsLabel();
            refreshData();
        });

        cmbYear = new JComboBox<>();
        cmbYear.setFont(UITheme.FONT_REGULAR);
        cmbYear.setBackground(Color.WHITE);
        academicYearDao.findAll().forEach(cmbYear::addItem);
        cmbYear.addActionListener(e -> refreshData());

        lblLimitsInfo = new JLabel("Scheme: Int Max: 40 | Ext Max: 60 | Pass: 40");
        lblLimitsInfo.setFont(UITheme.FONT_BOLD);
        lblLimitsInfo.setForeground(UITheme.PRIMARY_BLUE);

        topBar.add(new JLabel("Assigned Course:"));
        topBar.add(cmbSubject);
        topBar.add(new JLabel("Academic Year:"));
        topBar.add(cmbYear);
        topBar.add(Box.createHorizontalStrut(15));
        topBar.add(lblLimitsInfo);

        mainLayout.add(topBar, BorderLayout.NORTH);

        // Editable Table
        String[] cols = {"Student ID", "Roll No", "Student Name", "Internal Marks", "External Marks", "Total Marks", "Grade", "Points", "Result", "Absent", "Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Internal marks (3), External marks (4), Absent (9), Remarks (10) are editable
                return col == 3 || col == 4 || col == 9 || col == 10;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 9) return Boolean.class;
                return Object.class;
            }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        // Real-time calculation listener
        tableModel.addTableModelListener(e -> {
            if (isUpdatingTable || e.getType() != TableModelEvent.UPDATE) return;
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 3 || col == 4 || col == 9) {
                recalculateRow(row);
            }
        });

        mainLayout.add(StyledTable.wrapInScrollPane(table), BorderLayout.CENTER);
        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    private void loadCourses() {
        cmbSubject.removeAllItems();
        if (currentUser.getRole() == Role.ADMIN) {
            List<Subject> all = subjectService.getAllSubjects(null, null, true);
            all.forEach(s -> cmbSubject.addItem(new SubjectComboItem(s)));
        } else {
            var facOpt = facultyService.getFacultyByUserId(currentUser.getId());
            if (facOpt.isPresent()) {
                List<FacultySubject> list = subjectService.getFacultyAssignments(facOpt.get().getId(), null);
                for (FacultySubject fs : list) {
                    subjectService.getSubjectById(fs.getSubjectId()).ifPresent(s -> cmbSubject.addItem(new SubjectComboItem(s)));
                }
            }
        }
        updateLimitsLabel();
    }

    private void updateLimitsLabel() {
        SubjectComboItem item = (SubjectComboItem) cmbSubject.getSelectedItem();
        if (item != null) {
            Subject s = item.subject;
            lblLimitsInfo.setText(String.format("Evaluation Scheme: Int Max: %.0f | Ext Max: %.0f | Total Max: %.0f | Pass: %.0f",
                    s.getMaxInternalMarks(), s.getMaxExternalMarks(), s.getMaxMarks(), s.getPassMarks()));
        }
    }

    @Override
    public void refreshData() {
        SubjectComboItem item = (SubjectComboItem) cmbSubject.getSelectedItem();
        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        if (item == null || year == null) return;

        Subject sub = item.subject;
        setStatus("Loading student roster and existing scores for " + sub.getSubjectCode() + "...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<Student> students;
            private List<Marks> existingMarks;

            @Override
            protected Void doInBackground() {
                // Students enrolled in this department and semester
                students = studentService.getAllStudents(null, sub.getDepartment(), sub.getSemester(), true, 0, 500, currentUser);
                existingMarks = marksService.getMarksForSubject(sub.getId(), year.getId(), sub.getSemester(), currentUser);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    isUpdatingTable = true;
                    tableModel.setRowCount(0);

                    for (Student st : students) {
                        Marks m = existingMarks.stream()
                                .filter(x -> x.getStudentId() == st.getId())
                                .findFirst()
                                .orElse(null);

                        double intMarks = m != null ? m.getInternalMarks() : 0.0;
                        double extMarks = m != null ? m.getExternalMarks() : 0.0;
                        double total = intMarks + extMarks;
                        boolean absent = m != null && m.isAbsent();
                        boolean passed = m != null ? m.isPassed() : (total >= sub.getPassMarks());
                        String grade = m != null ? m.getGrade() : (total > 0 ? gradeService.calculateGrade(total, passed, absent) : "-");
                        double points = m != null ? m.getGradePoint() : gradeService.calculateGradePoint(grade);
                        String remarks = m != null && m.getRemarks() != null ? m.getRemarks() : "";

                        tableModel.addRow(new Object[]{
                                st.getId(),
                                st.getRollNumber(),
                                st.getFullName(),
                                intMarks,
                                extMarks,
                                total,
                                grade,
                                points,
                                absent ? "ABSENT" : (passed ? "PASS" : "FAIL"),
                                absent,
                                remarks
                        });
                    }
                    isUpdatingTable = false;
                    setStatus("Loaded " + students.size() + " students in cohort.");
                } catch (Exception ex) {
                    isUpdatingTable = false;
                    setStatus("Error loading class roster: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void recalculateRow(int row) {
        SubjectComboItem item = (SubjectComboItem) cmbSubject.getSelectedItem();
        if (item == null) return;
        Subject sub = item.subject;

        try {
            double internal = Double.parseDouble(tableModel.getValueAt(row, 3).toString().trim());
            double external = Double.parseDouble(tableModel.getValueAt(row, 4).toString().trim());
            Boolean absent = (Boolean) tableModel.getValueAt(row, 9);
            if (absent == null) absent = false;

            double total = absent ? 0.0 : (internal + external);
            boolean passed = !absent && (total >= sub.getPassMarks());
            double pct = sub.getMaxMarks() > 0 ? (total / sub.getMaxMarks()) * 100.0 : 0.0;
            String grade = gradeService.calculateGrade(pct, passed, absent);
            double points = gradeService.calculateGradePoint(grade);

            isUpdatingTable = true;
            tableModel.setValueAt(total, row, 5);
            tableModel.setValueAt(grade, row, 6);
            tableModel.setValueAt(points, row, 7);
            tableModel.setValueAt(absent ? "ABSENT" : (passed ? "PASS" : "FAIL"), row, 8);
            isUpdatingTable = false;
        } catch (NumberFormatException ignored) {}
    }

    private void saveMarksToDatabase() {
        SubjectComboItem item = (SubjectComboItem) cmbSubject.getSelectedItem();
        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        if (item == null || year == null) return;
        Subject sub = item.subject;

        List<Marks> list = new ArrayList<>();
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            try {
                int studentId = (int) tableModel.getValueAt(r, 0);
                double internal = Double.parseDouble(tableModel.getValueAt(r, 3).toString().trim());
                double external = Double.parseDouble(tableModel.getValueAt(r, 4).toString().trim());
                Boolean absent = (Boolean) tableModel.getValueAt(r, 9);
                String remarks = (String) tableModel.getValueAt(r, 10);

                Marks m = new Marks();
                m.setStudentId(studentId);
                m.setSubjectId(sub.getId());
                m.setAcademicYearId(year.getId());
                m.setSemester(sub.getSemester());
                m.setInternalMarks(internal);
                m.setExternalMarks(external);
                m.setAbsent(absent != null && absent);
                m.setRemarks(remarks);

                list.add(m);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid number entered at row " + (r + 1) + ": " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        setStatus("Saving validated marks to MySQL...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return marksService.saveBatchMarks(list, sub.getId(), year.getId(), sub.getSemester(), currentUser);
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Successfully saved marks for " + list.size() + " students.");
                    JOptionPane.showMessageDialog(MarksEntryPanel.this,
                            "Marks successfully recorded and validated for " + list.size() + " students!",
                            "Saved", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    setStatus("Save failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(MarksEntryPanel.this,
                            "Failed to save marks: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportMarksCsv() {
        SubjectComboItem item = (SubjectComboItem) cmbSubject.getSelectedItem();
        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        if (item == null || year == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(item.subject.getSubjectCode() + "_Marks_Sheet.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new ReportService().exportSubjectMarks(item.subject.getId(), year.getId(), item.subject.getSemester(),
                        item.subject.getSubjectCode(), chooser.getSelectedFile(), currentUser);
                JOptionPane.showMessageDialog(this, "Marks sheet exported to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
