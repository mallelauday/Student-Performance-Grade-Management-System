package com.university.grademanagement.ui.admin;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.AcademicYear;
import com.university.grademanagement.model.AnalyticsSummary;
import com.university.grademanagement.service.AnalyticsService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.MetricCard;
import com.university.grademanagement.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * University Administrator Executive Dashboard with metric cards and JFreeChart analytics.
 */
public class AdminDashboardPanel extends BasePanel {

    private final AnalyticsService analyticsService;
    private final AcademicYearDao academicYearDao;

    private MetricCard cardStudents;
    private MetricCard cardFaculty;
    private MetricCard cardSubjects;
    private MetricCard cardAvgPerformance;

    private JPanel chartsContainer;
    private Integer currentYearId;

    public AdminDashboardPanel() {
        super("Administrator Dashboard", "Overview of academic performance, institutional metrics, and analytics");
        this.analyticsService = new AnalyticsService();
        this.academicYearDao = new AcademicYearDaoImpl();

        initDashboardUI();
        refreshData();
    }

    private void initDashboardUI() {
        // Refresh button
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Data");
        btnRefresh.addActionListener(e -> refreshData());
        actionsPanel.add(btnRefresh);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. Metric Cards Row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        cardStudents = new MetricCard("Total Students", "0", "Enrolled candidates", UITheme.PRIMARY_BLUE);
        cardFaculty = new MetricCard("Total Faculty", "0", "Academic instructors", UITheme.ACCENT_CYAN);
        cardSubjects = new MetricCard("Active Courses", "0", "Accredited syllabus", UITheme.SUCCESS_GREEN);
        cardAvgPerformance = new MetricCard("Class Average", "0.0%", "Overall score", UITheme.WARNING_AMBER);

        cardsRow.add(cardStudents);
        cardsRow.add(cardFaculty);
        cardsRow.add(cardSubjects);
        cardsRow.add(cardAvgPerformance);

        mainLayout.add(cardsRow);
        mainLayout.add(Box.createVerticalStrut(20));

        // 2. Charts Container (Grade Distribution & Subject Averages)
        chartsContainer = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsContainer.setOpaque(false);
        chartsContainer.setPreferredSize(new Dimension(800, 340));

        mainLayout.add(chartsContainer);
        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading analytics metrics from database...");
        SwingWorker<AnalyticsSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalyticsSummary doInBackground() {
                academicYearDao.getCurrentYear().ifPresent(y -> currentYearId = y.getId());
                return analyticsService.getAnalyticsSummary(currentYearId, 5); // default semester 5
            }

            @Override
            protected void done() {
                try {
                    AnalyticsSummary data = get();
                    cardStudents.update(String.valueOf(data.getTotalStudents()), "Active enrollments");
                    cardFaculty.update(String.valueOf(data.getTotalFaculty()), "Teaching staff");
                    cardSubjects.update(String.valueOf(data.getTotalSubjects()), "Active curriculum");
                    cardAvgPerformance.update(String.format("%.1f%%", data.getClassAveragePercentage()),
                            "Pass rate: " + String.format("%.1f%%", data.getOverallPassRate()));

                    updateCharts(data);
                    setStatus("Dashboard updated successfully.");
                } catch (Exception e) {
                    setStatus("Error loading dashboard metrics: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCharts(AnalyticsSummary data) {
        chartsContainer.removeAll();

        // 1. Grade Distribution Bar Chart
        DefaultCategoryDataset gradeDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : data.getGradeDistribution().entrySet()) {
            gradeDataset.addValue(entry.getValue(), "Students", entry.getKey());
        }

        JFreeChart gradeChart = ChartFactory.createBarChart(
                "Grade Distribution (Cohort)",
                "Grade Scale",
                "Number of Students",
                gradeDataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        styleBarChart(gradeChart, UITheme.PRIMARY_BLUE);
        ChartPanel panel1 = new ChartPanel(gradeChart);
        panel1.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        panel1.setBackground(Color.WHITE);

        // 2. Subject Wise Average Performance
        DefaultCategoryDataset subDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.getSubjectAverages().entrySet()) {
            String shortCode = entry.getKey().contains(" - ") ? entry.getKey().split(" - ")[0] : entry.getKey();
            subDataset.addValue(entry.getValue(), "Average Score", shortCode);
        }

        JFreeChart subChart = ChartFactory.createBarChart(
                "Subject Average Scores",
                "Course Code",
                "Average Marks (out of 100)",
                subDataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        styleBarChart(subChart, UITheme.SUCCESS_GREEN);
        ChartPanel panel2 = new ChartPanel(subChart);
        panel2.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        panel2.setBackground(Color.WHITE);

        chartsContainer.add(panel1);
        chartsContainer.add(panel2);
        chartsContainer.revalidate();
        chartsContainer.repaint();
    }

    private void styleBarChart(JFreeChart chart, Color barColor) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(UITheme.FONT_HEADER_SMALL);
        chart.getTitle().setPaint(UITheme.TEXT_PRIMARY);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, barColor);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.1);
    }
}
