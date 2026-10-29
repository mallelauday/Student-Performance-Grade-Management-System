package com.university.grademanagement.ui.student;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.dao.StudentDao;
import com.university.grademanagement.dao.StudentDaoImpl;
import com.university.grademanagement.model.*;
import com.university.grademanagement.service.GradeService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Student Academic Progress and Performance Trends Analytics Panel.
 */
public class PerformanceTrendsPanel extends BasePanel {

    private final StudentDao studentDao;
    private final AcademicYearDao academicYearDao;
    private final GradeService gradeService;
    private final User currentUser;

    private JPanel chartWrapper;

    public PerformanceTrendsPanel(User currentUser) {
        super("Academic Performance Trends", "Visual analytics of course strengths, score breakdowns, and academic trajectory");
        this.studentDao = new StudentDaoImpl();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.gradeService = new GradeService();
        this.currentUser = currentUser;

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Analytics");
        btnRefresh.addActionListener(e -> refreshData());
        actionsPanel.add(btnRefresh);

        chartWrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        chartWrapper.setOpaque(false);

        contentPanel.add(chartWrapper, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Generating personal performance charts...");
        SwingWorker<GradeResult, Void> worker = new SwingWorker<>() {
            @Override
            protected GradeResult doInBackground() {
                Optional<Student> sOpt = studentDao.findByUserId(currentUser.getId());
                if (sOpt.isEmpty()) return null;
                Student s = sOpt.get();

                AcademicYear year = academicYearDao.getCurrentYear().orElse(null);
                if (year == null) return null;

                return gradeService.calculateSemesterResult(s.getId(), year.getId(), s.getCurrentSemester());
            }

            @Override
            protected void done() {
                try {
                    GradeResult result = get();
                    if (result == null) {
                        setStatus("No academic trends data available.");
                        return;
                    }

                    renderCharts(result);
                    setStatus("Performance trends rendered.");
                } catch (Exception ex) {
                    setStatus("Error rendering trends: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderCharts(GradeResult result) {
        chartWrapper.removeAll();

        // 1. Total Marks per Subject vs Passing Benchmark
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        for (SubjectResult sr : result.getSubjectResults()) {
            ds1.addValue(sr.getTotalMarks(), "Your Marks", sr.getSubjectCode());
            ds1.addValue(40.0, "Pass Benchmark", sr.getSubjectCode());
        }

        JFreeChart chart1 = ChartFactory.createBarChart(
                "Course Performance vs Pass Benchmark",
                "Course Code",
                "Marks (out of 100)",
                ds1,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chart1.setBackgroundPaint(Color.WHITE);
        chart1.getTitle().setFont(UITheme.FONT_HEADER_SMALL);

        ChartPanel cp1 = new ChartPanel(chart1);
        cp1.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        // 2. Internal vs External Breakdown
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        for (SubjectResult sr : result.getSubjectResults()) {
            ds2.addValue(sr.getInternalMarks(), "Internal Marks (Max 40)", sr.getSubjectCode());
            ds2.addValue(sr.getExternalMarks(), "External Marks (Max 60)", sr.getSubjectCode());
        }

        JFreeChart chart2 = ChartFactory.createBarChart(
                "Assessment Component Breakdown",
                "Course Code",
                "Component Score",
                ds2,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chart2.setBackgroundPaint(Color.WHITE);
        chart2.getTitle().setFont(UITheme.FONT_HEADER_SMALL);

        ChartPanel cp2 = new ChartPanel(chart2);
        cp2.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        chartWrapper.add(cp1);
        chartWrapper.add(cp2);
        chartWrapper.revalidate();
        chartWrapper.repaint();
    }
}
