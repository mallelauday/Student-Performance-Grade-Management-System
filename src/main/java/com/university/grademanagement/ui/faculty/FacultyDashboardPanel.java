package com.university.grademanagement.ui.faculty;

import com.university.grademanagement.dao.AcademicYearDao;
import com.university.grademanagement.dao.AcademicYearDaoImpl;
import com.university.grademanagement.model.AcademicYear;
import com.university.grademanagement.model.Faculty;
import com.university.grademanagement.model.FacultySubject;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.FacultyService;
import com.university.grademanagement.service.SubjectService;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.MetricCard;
import com.university.grademanagement.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Faculty Dashboard Overview Panel displaying assigned teaching assignments and evaluation deadlines.
 */
public class FacultyDashboardPanel extends BasePanel {

    private final FacultyService facultyService;
    private final SubjectService subjectService;
    private final AcademicYearDao academicYearDao;
    private final User currentUser;

    private MetricCard cardAssignedCourses;
    private MetricCard cardDepartment;
    private MetricCard cardAcademicPeriod;
    private JPanel coursesPanel;

    public FacultyDashboardPanel(User currentUser) {
        super("Faculty Workspace", "Welcome, " + currentUser.getFullName() + " | Assigned courses and evaluation portal");
        this.facultyService = new FacultyService();
        this.subjectService = new SubjectService();
        this.academicYearDao = new AcademicYearDaoImpl();
        this.currentUser = currentUser;

        initUI();
        refreshData();
    }

    private void initUI() {
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());
        actionsPanel.add(btnRefresh);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // Metric cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        cardAssignedCourses = new MetricCard("Assigned Courses", "0", "Current semester courses", UITheme.PRIMARY_BLUE);
        cardDepartment = new MetricCard("Department", "CSE", "Faculty Division", UITheme.ACCENT_CYAN);
        cardAcademicPeriod = new MetricCard("Academic Session", "2025-2026", "Active period", UITheme.SUCCESS_GREEN);

        cardsRow.add(cardAssignedCourses);
        cardsRow.add(cardDepartment);
        cardsRow.add(cardAcademicPeriod);

        mainLayout.add(cardsRow);
        mainLayout.add(Box.createVerticalStrut(20));

        // Assigned Courses Card Container
        JLabel lblHeader = new JLabel("Your Assigned Teaching Courses");
        lblHeader.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblHeader.setForeground(UITheme.TEXT_PRIMARY);

        coursesPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        coursesPanel.setOpaque(false);

        mainLayout.add(lblHeader);
        mainLayout.add(Box.createVerticalStrut(10));
        mainLayout.add(coursesPanel);

        contentPanel.add(mainLayout, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        setStatus("Loading faculty assignments...");
        SwingWorker<List<FacultySubject>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FacultySubject> doInBackground() {
                var facOpt = facultyService.getFacultyByUserId(currentUser.getId());
                if (facOpt.isEmpty()) return List.of();
                Faculty f = facOpt.get();
                cardDepartment.update(f.getDepartment(), f.getDesignation());
                return subjectService.getFacultyAssignments(f.getId(), null);
            }

            @Override
            protected void done() {
                try {
                    List<FacultySubject> assignments = get();
                    cardAssignedCourses.update(String.valueOf(assignments.size()), "Allocated courses");
                    academicYearDao.getCurrentYear().ifPresent(y -> cardAcademicPeriod.update(y.getYearCode(), y.getYearName()));

                    coursesPanel.removeAll();
                    for (FacultySubject fs : assignments) {
                        JPanel cCard = createCourseCard(fs);
                        coursesPanel.add(cCard);
                    }
                    coursesPanel.revalidate();
                    coursesPanel.repaint();
                    setStatus("Loaded " + assignments.size() + " teaching assignments.");
                } catch (Exception ex) {
                    setStatus("Error loading faculty dashboard: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createCourseCard(FacultySubject fs) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.CARD_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel code = new JLabel(fs.getSubjectCode());
        code.setFont(UITheme.FONT_HEADER_MEDIUM);
        code.setForeground(UITheme.PRIMARY_BLUE);

        JLabel sem = new JLabel("Semester " + fs.getSemester());
        sem.setFont(UITheme.FONT_BOLD);
        sem.setForeground(UITheme.TEXT_MUTED);

        top.add(code, BorderLayout.WEST);
        top.add(sem, BorderLayout.EAST);

        JLabel name = new JLabel(fs.getSubjectName());
        name.setFont(UITheme.FONT_HEADER_SMALL);
        name.setForeground(UITheme.TEXT_PRIMARY);

        JLabel period = new JLabel("Academic Session: " + fs.getAcademicYearCode());
        period.setFont(UITheme.FONT_SMALL);
        period.setForeground(UITheme.TEXT_MUTED);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(8, 0, 8, 0));
        center.add(name);
        center.add(Box.createVerticalStrut(4));
        center.add(period);

        p.add(top, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);

        return p;
    }
}
