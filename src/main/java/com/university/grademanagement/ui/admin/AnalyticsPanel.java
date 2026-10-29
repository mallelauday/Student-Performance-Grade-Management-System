package com.university.grademanagement.ui.admin;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.AcademicYear;
import com.university.grademanagement.model.AnalyticsSummary;
import com.university.grademanagement.model.StudentRank;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.AnalyticsService;
import com.university.grademanagement.service.RankingService;
import com.university.grademanagement.service.ReportService;
import com.university.grademanagement.ui.common.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Institutional Analytics & Merit Ranking Panel with JFreeChart analytics and PDF export.
 */
public class AnalyticsPanel extends BasePanel {

    private final AnalyticsService analyticsService;
    private final RankingService rankingService;
    private final ReportService reportService;
    private final AcademicYearDao academicYearDao;
    private final User currentUser;

    private JComboBox<AcademicYear> cmbYear;
    private JComboBox<Integer> cmbSem;
    private JComboBox<String> cmbDept;

    private MetricCard cardAvg;
    private MetricCard cardPassRate;
    private MetricCard cardHighest;
    private MetricCard cardAtRisk;

    private StyledTable rankingTable;
    private DefaultTableModel rankingModel;
    private List<StudentRank> currentRankings;

    private StyledTable atRiskTable;
    private DefaultTableModel atRiskModel;

    private JPanel chartWrapper;

    public AnalyticsPanel(User currentUser) {
        super("Institutional Analytics & Academic Rankings", "Class-wide performance distributions, merit ranking, and at-risk student monitoring");
        this.analyticsService = new AnalyticsService();
        this.rankingService = new RankingService();
        this.reportService = new ReportService();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.currentUser = currentUser;

        initUI();
        refreshData();
    }

    private void initUI() {
        // Actions
        JButton btnExportPdf = UITheme.createPrimaryButton("Export Merit List (PDF)");
        btnExportPdf.addActionListener(e -> exportRankingsPdf());

        JButton btnExportCsv = UITheme.createSecondaryButton("Export Rankings (CSV)");
        btnExportCsv.addActionListener(e -> exportRankingsCsv());

        actionsPanel.add(btnExportPdf);
        actionsPanel.add(btnExportCsv);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // Filter toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        cmbYear = new JComboBox<>();
        academicYearDao.findAll().forEach(cmbYear::addItem);
        cmbYear.addActionListener(e -> refreshData());

        cmbSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cmbSem.setSelectedItem(5);
        cmbSem.addActionListener(e -> refreshData());

        cmbDept = new JComboBox<>(new String[]{"All Departments", "Computer Science & Engineering", "Information Technology"});
        cmbDept.addActionListener(e -> refreshData());

        JButton btnApply = UITheme.createSecondaryButton("Apply Filters");
        btnApply.addActionListener(e -> refreshData());

        filterBar.add(new JLabel("Academic Year:"));
        filterBar.add(cmbYear);
        filterBar.add(new JLabel("Semester:"));
        filterBar.add(cmbSem);
        filterBar.add(new JLabel("Department:"));
        filterBar.add(cmbDept);
        filterBar.add(btnApply);

        mainLayout.add(filterBar);
        mainLayout.add(Box.createVerticalStrut(14));

        // Metric cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        cardAvg = new MetricCard("Class Average", "0.0%", "Overall score", UITheme.PRIMARY_BLUE);
        cardPassRate = new MetricCard("Overall Pass Rate", "0.0%", "Subject success", UITheme.SUCCESS_GREEN);
        cardHighest = new MetricCard("Highest Percentage", "0.0%", "Top score", UITheme.ACCENT_CYAN);
        cardAtRisk = new MetricCard("Students At-Risk", "0", "Failed / low scores", UITheme.DANGER_RED);

        cardsRow.add(cardAvg);
        cardsRow.add(cardPassRate);
        cardsRow.add(cardHighest);
        cardsRow.add(cardAtRisk);

        mainLayout.add(cardsRow);
        mainLayout.add(Box.createVerticalStrut(14));

        // Tabbed Panel for Rankings, Charts, and At-Risk
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        // Tab 1: Rankings
        String[] rCols = {"Rank", "Roll Number", "Student Name", "Department", "Sem", "Total Marks", "Max Marks", "Percentage", "SGPA", "Status"};
        rankingModel = new DefaultTableModel(rCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rankingTable = new StyledTable(rankingModel);
        rankingTable.getColumnModel().getColumn(0).setMaxWidth(55);
        rankingTable.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());
        tabbedPane.addTab("Academic Merit Rankings", StyledTable.wrapInScrollPane(rankingTable));

        // Tab 2: Visual Charts
        chartWrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        chartWrapper.setBackground(Color.WHITE);
        chartWrapper.setBorder(new EmptyBorder(12, 12, 12, 12));
        tabbedPane.addTab("Performance Charts & Distributions", chartWrapper);

        // Tab 3: At-Risk Students
        String[] aCols = {"#", "Roll Number", "Student Name", "Department", "Sem", "Total Marks", "Percentage", "SGPA", "Flag / Academic Concern"};
        atRiskModel = new DefaultTableModel(aCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        atRiskTable = new StyledTable(atRiskModel);
        atRiskTable.getColumnModel().getColumn(0).setMaxWidth(40);
        atRiskTable.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());
        tabbedPane.addTab("Students Requiring Support (At-Risk)", StyledTable.wrapInScrollPane(atRiskTable));

        mainLayout.add(tabbedPane);
        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        if (year == null) return;
        int sem = (Integer) cmbSem.getSelectedItem();
        String dept = (String) cmbDept.getSelectedItem();
        if ("All Departments".equals(dept)) dept = null;

        setStatus("Calculating institutional analytics and cohort rankings...");

        String finalDept = dept;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private AnalyticsSummary summary;
            private List<StudentRank> rankings;
            private List<StudentRank> atRisk;

            @Override
            protected Void doInBackground() {
                summary = analyticsService.getAnalyticsSummary(year.getId(), sem);
                rankings = rankingService.getClassRankings(year.getId(), sem, finalDept, currentUser);
                atRisk = rankingService.getAtRiskStudents(year.getId(), sem, currentUser);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    currentRankings = rankings;

                    // Update cards
                    cardAvg.update(String.format("%.1f%%", summary.getClassAveragePercentage()), "Mean cohort percentage");
                    cardPassRate.update(String.format("%.1f%%", summary.getOverallPassRate()), "Evaluation clear rate");
                    cardHighest.update(String.format("%.1f%%", summary.getHighestPercentage()),
                            summary.getTopPerformerRoll() != null ? "Top: " + summary.getTopPerformerRoll() : "Top score");
                    cardAtRisk.update(String.valueOf(atRisk.size()), "Requires counseling / re-exam");

                    // Populate rankings table
                    rankingModel.setRowCount(0);
                    for (StudentRank r : rankings) {
                        rankingModel.addRow(new Object[]{
                                r.getRank() > 0 ? String.valueOf(r.getRank()) : "-",
                                r.getRollNumber(),
                                r.getFullName(),
                                r.getDepartment(),
                                "Sem " + r.getSemester(),
                                String.format("%.1f", r.getTotalMarks()),
                                String.format("%.0f", r.getMaxMarks()),
                                String.format("%.2f%%", r.getPercentage()),
                                String.format("%.2f", r.getSgpa()),
                                r.getStatus()
                        });
                    }

                    // Populate at-risk table
                    atRiskModel.setRowCount(0);
                    int idx = 1;
                    for (StudentRank a : atRisk) {
                        atRiskModel.addRow(new Object[]{
                                idx++,
                                a.getRollNumber(),
                                a.getFullName(),
                                a.getDepartment(),
                                "Sem " + a.getSemester(),
                                String.format("%.1f", a.getTotalMarks()),
                                String.format("%.2f%%", a.getPercentage()),
                                String.format("%.2f", a.getSgpa()),
                                a.getStatus()
                        });
                    }

                    // Populate charts
                    updateCharts(summary);
                    setStatus("Rankings and metrics updated successfully.");
                } catch (Exception ex) {
                    setStatus("Error loading analytics: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCharts(AnalyticsSummary summary) {
        chartWrapper.removeAll();

        // 1. Grade Distribution Pie Chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : summary.getGradeDistribution().entrySet()) {
            if (entry.getValue() > 0) {
                pieDataset.setValue("Grade " + entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            }
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Grade Breakdown",
                pieDataset,
                true, true, false
        );
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChart.getTitle().setFont(UITheme.FONT_HEADER_SMALL);
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        ChartPanel p1 = new ChartPanel(pieChart);
        p1.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        chartWrapper.add(p1);
        chartWrapper.revalidate();
        chartWrapper.repaint();
    }

    private void exportRankingsPdf() {
        if (currentRankings == null || currentRankings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No rankings available to export.", "Empty Cohort", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        int sem = (Integer) cmbSem.getSelectedItem();
        String dept = (String) cmbDept.getSelectedItem();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Class_Rankings_Sem" + sem + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportClassRankings(year.getId(), sem, dept, year.getYearCode(), chooser.getSelectedFile(), "PDF", currentUser);
                JOptionPane.showMessageDialog(this, "Merit rankings PDF exported successfully to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRankingsCsv() {
        if (currentRankings == null || currentRankings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No rankings available to export.", "Empty Cohort", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        int sem = (Integer) cmbSem.getSelectedItem();
        String dept = (String) cmbDept.getSelectedItem();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Class_Rankings_Sem" + sem + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportClassRankings(year.getId(), sem, dept, year.getYearCode(), chooser.getSelectedFile(), "CSV", currentUser);
                JOptionPane.showMessageDialog(this, "Rankings CSV exported successfully to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
