package com.university.grademanagement.ui.student;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.dao.StudentDao;
import com.university.grademanagement.dao.StudentDaoImpl;
import com.university.grademanagement.model.*;
import com.university.grademanagement.service.GradeService;
import com.university.grademanagement.service.ReportService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.MetricCard;
import com.university.grademanagement.ui.common.StatusBadgeRenderer;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * Student Academic Dashboard Panel presenting evaluated marks, SGPA, credits, and PDF export.
 */
public class StudentDashboardPanel extends BasePanel {

    private final StudentDao studentDao;
    private final AcademicYearDao academicYearDao;
    private final GradeService gradeService;
    private final ReportService reportService;
    private final User currentUser;

    private Student studentProfile;
    private AcademicYear currentYear;
    private GradeResult currentResult;

    private MetricCard cardPercentage;
    private MetricCard cardSgpa;
    private MetricCard cardCredits;
    private MetricCard cardStatus;

    private StyledTable table;
    private DefaultTableModel tableModel;

    public StudentDashboardPanel(User currentUser) {
        super("Academic Performance Portal", "Welcome, " + currentUser.getFullName() + " | Official semester grades and grade card");
        this.studentDao = new StudentDaoImpl();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.gradeService = new GradeService();
        this.reportService = new ReportService();
        this.currentUser = currentUser;

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnExportPdf = UITheme.createPrimaryButton("Download Marksheet (PDF)");
        btnExportPdf.addActionListener(e -> downloadMarksheetPdf());

        JButton btnExportCsv = UITheme.createSecondaryButton("Export Marksheet (CSV)");
        btnExportCsv.addActionListener(e -> downloadMarksheetCsv());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        actionsPanel.add(btnExportPdf);
        actionsPanel.add(btnExportCsv);
        actionsPanel.add(btnRefresh);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // Metric cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        cardPercentage = new MetricCard("Overall Score", "0.0%", "Semester average", UITheme.PRIMARY_BLUE);
        cardSgpa = new MetricCard("Semester SGPA", "0.00", "10-point scale", UITheme.ACCENT_CYAN);
        cardCredits = new MetricCard("Credits Registered", "0", "Accredited units", UITheme.SUCCESS_GREEN);
        cardStatus = new MetricCard("Academic Result", "PENDING", "Standing", UITheme.WARNING_AMBER);

        cardsRow.add(cardPercentage);
        cardsRow.add(cardSgpa);
        cardsRow.add(cardCredits);
        cardsRow.add(cardStatus);

        mainLayout.add(cardsRow);
        mainLayout.add(Box.createVerticalStrut(20));

        // Marksheet Table
        JLabel lblTableTitle = new JLabel("Semester 5 Course Evaluation Breakdown");
        lblTableTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblTableTitle.setForeground(UITheme.TEXT_PRIMARY);

        String[] cols = {"Course Code", "Course Title", "Credits", "Internal Marks", "External Marks", "Total Marks", "Max Marks", "Grade", "Points", "Result"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(95);
        table.getColumnModel().getColumn(2).setMaxWidth(60);
        table.getColumnModel().getColumn(7).setMaxWidth(65);
        table.getColumnModel().getColumn(8).setMaxWidth(65);
        table.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());

        mainLayout.add(lblTableTitle);
        mainLayout.add(Box.createVerticalStrut(8));
        mainLayout.add(StyledTable.wrapInScrollPane(table));

        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading student grade report...");
        SwingWorker<GradeResult, Void> worker = new SwingWorker<>() {
            @Override
            protected GradeResult doInBackground() {
                Optional<Student> sOpt = studentDao.findByUserId(currentUser.getId());
                if (sOpt.isEmpty()) return null;
                studentProfile = sOpt.get();

                currentYear = academicYearDao.getCurrentYear().orElse(null);
                if (currentYear == null) return null;

                return gradeService.calculateSemesterResult(studentProfile.getId(), currentYear.getId(), studentProfile.getCurrentSemester());
            }

            @Override
            protected void done() {
                try {
                    currentResult = get();
                    if (currentResult == null) {
                        setStatus("No academic records found for this student account.");
                        return;
                    }

                    cardPercentage.update(String.format("%.2f%%", currentResult.getPercentage()), "Total marks: " + (int)currentResult.getTotalObtainedMarks() + "/" + (int)currentResult.getTotalMaxMarks());
                    cardSgpa.update(String.format("%.2f", currentResult.getSgpa()), "Grade: " + currentResult.getOverallGrade());
                    cardCredits.update(String.valueOf(currentResult.getTotalCredits()), "Credits earned");
                    cardStatus.update(currentResult.getStatus(), currentResult.getRemarks());

                    if (currentResult.isPassed()) {
                        cardStatus.setAccentColor(UITheme.SUCCESS_GREEN);
                    } else if (currentResult.isIncomplete()) {
                        cardStatus.setAccentColor(UITheme.WARNING_AMBER);
                    } else {
                        cardStatus.setAccentColor(UITheme.DANGER_RED);
                    }

                    tableModel.setRowCount(0);
                    for (SubjectResult sr : currentResult.getSubjectResults()) {
                        tableModel.addRow(new Object[]{
                                sr.getSubjectCode(),
                                sr.getSubjectName(),
                                sr.getCredits(),
                                String.format("%.1f", sr.getInternalMarks()),
                                String.format("%.1f", sr.getExternalMarks()),
                                String.format("%.1f", sr.getTotalMarks()),
                                String.format("%.0f", sr.getMaxMarks()),
                                sr.getGrade(),
                                String.format("%.1f", sr.getGradePoint()),
                                sr.isPassed() ? "PASS" : (sr.isAbsent() ? "ABSENT" : "FAIL")
                        });
                    }

                    setStatus("Academic marksheet loaded for " + studentProfile.getRollNumber() + ".");
                } catch (Exception ex) {
                    setStatus("Error loading marksheet: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void downloadMarksheetPdf() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No marksheet available to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(studentProfile.getRollNumber() + "_Marksheet_Sem" + studentProfile.getCurrentSemester() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportStudentMarksheet(studentProfile.getId(), currentYear.getId(), studentProfile.getCurrentSemester(), chooser.getSelectedFile(), "PDF", currentUser);
                JOptionPane.showMessageDialog(this, "Official Marksheet PDF generated successfully:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void downloadMarksheetCsv() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No marksheet available to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(studentProfile.getRollNumber() + "_Marksheet_Sem" + studentProfile.getCurrentSemester() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportStudentMarksheet(studentProfile.getId(), currentYear.getId(), studentProfile.getCurrentSemester(), chooser.getSelectedFile(), "CSV", currentUser);
                JOptionPane.showMessageDialog(this, "Marksheet exported to CSV:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
