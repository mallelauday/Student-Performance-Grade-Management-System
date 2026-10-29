package com.university.grademanagement.ui.faculty;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.*;
import com.university.grademanagement.service.FacultyService;
import com.university.grademanagement.service.MarksService;
import com.university.grademanagement.service.SubjectService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.MetricCard;
import com.university.grademanagement.ui.common.StatusBadgeRenderer;
import com.university.grademanagement.ui.common.StyledTable;
import com.university.grademanagement.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Faculty Class Performance Analysis Panel with score distributions and student intervention lists.
 */
public class ClassPerformancePanel extends BasePanel {

    private final MarksService marksService;
    private final SubjectService subjectService;
    private final FacultyService facultyService;
    private final AcademicYearDao academicYearDao;
    private final User currentUser;

    private JComboBox<Subject> cmbSubject;
    private JComboBox<AcademicYear> cmbYear;

    private MetricCard cardEnrolled;
    private MetricCard cardAvgScore;
    private MetricCard cardPassRate;
    private MetricCard cardAtRisk;

    private JPanel chartPanel;
    private StyledTable tableAtRisk;
    private DefaultTableModel atRiskModel;

    public ClassPerformancePanel(User currentUser) {
        super("Class Performance Analysis", "Subject-specific score distribution, grade brackets, and intervention tracking");
        this.marksService = new MarksService();
        this.subjectService = new SubjectService();
        this.facultyService = new FacultyService();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.currentUser = currentUser;

        initUI();
        loadCourses();
    }

    private void initUI() {
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Analysis");
        btnRefresh.addActionListener(e -> refreshData());
        actionsPanel.add(btnRefresh);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // Selection bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        cmbSubject = new JComboBox<>();
        cmbSubject.setFont(UITheme.FONT_REGULAR);
        cmbSubject.setBackground(Color.WHITE);
        cmbSubject.addActionListener(e -> refreshData());

        cmbYear = new JComboBox<>();
        cmbYear.setFont(UITheme.FONT_REGULAR);
        cmbYear.setBackground(Color.WHITE);
        academicYearDao.findAll().forEach(cmbYear::addItem);
        cmbYear.addActionListener(e -> refreshData());

        topBar.add(new JLabel("Assigned Course:"));
        topBar.add(cmbSubject);
        topBar.add(new JLabel("Academic Period:"));
        topBar.add(cmbYear);

        mainLayout.add(topBar);
        mainLayout.add(Box.createVerticalStrut(14));

        // Metric cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        cardEnrolled = new MetricCard("Evaluated Students", "0", "Records scored", UITheme.PRIMARY_BLUE);
        cardAvgScore = new MetricCard("Average Score", "0.0", "Mean out of 100", UITheme.ACCENT_CYAN);
        cardPassRate = new MetricCard("Subject Pass Rate", "0.0%", "Pass criteria met", UITheme.SUCCESS_GREEN);
        cardAtRisk = new MetricCard("Students Requiring Support", "0", "Failed / below 40", UITheme.DANGER_RED);

        cardsRow.add(cardEnrolled);
        cardsRow.add(cardAvgScore);
        cardsRow.add(cardPassRate);
        cardsRow.add(cardAtRisk);

        mainLayout.add(cardsRow);
        mainLayout.add(Box.createVerticalStrut(14));

        // Split visual: Chart on left, At-Risk table on right
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        splitPanel.setOpaque(false);

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        JLabel lblAtRiskTitle = new JLabel("Students Needing Support / Failed in Course");
        lblAtRiskTitle.setFont(UITheme.FONT_HEADER_SMALL);
        lblAtRiskTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblAtRiskTitle.setBorder(new EmptyBorder(10, 12, 10, 12));

        String[] cols = {"Roll Number", "Student Name", "Internal", "External", "Total", "Grade", "Status"};
        atRiskModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAtRisk = new StyledTable(atRiskModel);
        tableAtRisk.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        rightPanel.add(lblAtRiskTitle, BorderLayout.NORTH);
        rightPanel.add(StyledTable.wrapInScrollPane(tableAtRisk), BorderLayout.CENTER);

        splitPanel.add(chartPanel);
        splitPanel.add(rightPanel);

        mainLayout.add(splitPanel);
        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    private void loadCourses() {
        cmbSubject.removeAllItems();
        if (currentUser.getRole() == Role.ADMIN) {
            subjectService.getAllSubjects(null, null, true).forEach(cmbSubject::addItem);
        } else {
            var facOpt = facultyService.getFacultyByUserId(currentUser.getId());
            if (facOpt.isPresent()) {
                List<FacultySubject> list = subjectService.getFacultyAssignments(facOpt.get().getId(), null);
                for (FacultySubject fs : list) {
                    subjectService.getSubjectById(fs.getSubjectId()).ifPresent(cmbSubject::addItem);
                }
            }
        }
    }

    @Override
    public void refreshData() {
        Subject sub = (Subject) cmbSubject.getSelectedItem();
        AcademicYear year = (AcademicYear) cmbYear.getSelectedItem();
        if (sub == null || year == null) return;

        setStatus("Analyzing class performance for " + sub.getSubjectCode() + "...");
        SwingWorker<List<Marks>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Marks> doInBackground() {
                return marksService.getMarksForSubject(sub.getId(), year.getId(), sub.getSemester(), currentUser);
            }

            @Override
            protected void done() {
                try {
                    List<Marks> marksList = get();
                    int total = marksList.size();
                    int passed = 0;
                    double sum = 0.0;
                    int atRiskCount = 0;

                    atRiskModel.setRowCount(0);
                    int[] brackets = new int[5]; // 90-100, 75-89, 60-74, 40-59, <40

                    for (Marks m : marksList) {
                        double tot = m.getTotalMarks();
                        sum += tot;
                        if (m.isPassed()) {
                            passed++;
                        }
                        if (!m.isPassed() || tot < 45.0) {
                            atRiskCount++;
                            atRiskModel.addRow(new Object[]{
                                    m.getStudentRollNumber(),
                                    m.getStudentName(),
                                    String.format("%.1f", m.getInternalMarks()),
                                    String.format("%.1f", m.getExternalMarks()),
                                    String.format("%.1f", m.getTotalMarks()),
                                    m.getGrade(),
                                    m.isPassed() ? "Low Pass" : "Failed"
                            });
                        }

                        if (tot >= 90) brackets[0]++;
                        else if (tot >= 75) brackets[1]++;
                        else if (tot >= 60) brackets[2]++;
                        else if (tot >= 40) brackets[3]++;
                        else brackets[4]++;
                    }

                    double avg = total > 0 ? (sum / total) : 0.0;
                    double passRate = total > 0 ? ((double) passed / total) * 100.0 : 0.0;

                    cardEnrolled.update(String.valueOf(total), "Evaluated students");
                    cardAvgScore.update(String.format("%.1f", avg), "Class mean");
                    cardPassRate.update(String.format("%.1f%%", passRate), passed + " of " + total + " passed");
                    cardAtRisk.update(String.valueOf(atRiskCount), "Needing remedial help");

                    updateChart(sub.getSubjectCode(), brackets);
                    setStatus("Performance analysis complete for " + sub.getSubjectCode() + ".");
                } catch (Exception ex) {
                    setStatus("Error analyzing performance: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateChart(String subjectCode, int[] brackets) {
        chartPanel.removeAll();

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(brackets[0], "Students", "90-100%");
        ds.addValue(brackets[1], "Students", "75-89%");
        ds.addValue(brackets[2], "Students", "60-74%");
        ds.addValue(brackets[3], "Students", "40-59%");
        ds.addValue(brackets[4], "Students", "<40% (Fail)");

        JFreeChart chart = ChartFactory.createBarChart(
                subjectCode + " Marks Range Distribution",
                "Score Range",
                "Students",
                ds,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(UITheme.FONT_HEADER_SMALL);

        ChartPanel cp = new ChartPanel(chart);
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}
