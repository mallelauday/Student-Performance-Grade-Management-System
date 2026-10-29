package com.university.grademanagement.ui;

import com.university.grademanagement.config.AppConfig;
import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.model.Role;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.AuthService;
import com.university.grademanagement.ui.admin.*;
import com.university.grademanagement.ui.common.BasePanel;
import com.university.grademanagement.ui.common.UITheme;
import com.university.grademanagement.ui.faculty.*;
import com.university.grademanagement.ui.student.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary Application Window featuring a role-specific dark sidebar navigation,
 * header status bar, and dynamic CardLayout workspace.
 */
public class MainFrame extends JFrame {

    private final User currentUser;
    private final AuthService authService;

    private JPanel sidebarPanel;
    private JPanel contentContainer;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private final Map<String, BasePanel> panels = new HashMap<>();
    private String activeNavKey = "";

    public MainFrame(User user) {
        super(AppConfig.APP_NAME + " - [" + user.getRole().getDisplayName() + "]");
        this.currentUser = user;
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        setSize(1240, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // 1. Left Sidebar
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(UITheme.NAVY_DARK);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));

        // Brand & User Chip
        JPanel topSidebar = new JPanel();
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));
        topSidebar.setOpaque(false);
        topSidebar.setBorder(new EmptyBorder(24, 18, 20, 18));

        JLabel lblBrand = new JLabel("APEX UNIVERSITY");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblApp = new JLabel("Grade Management System");
        lblApp.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblApp.setForeground(new Color(148, 163, 184));

        // User Chip
        JPanel userChip = new JPanel();
        userChip.setLayout(new BoxLayout(userChip, BoxLayout.Y_AXIS));
        userChip.setBackground(UITheme.NAVY_SURFACE);
        userChip.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(51, 65, 85)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblName = new JLabel(currentUser.getFullName());
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(currentUser.getRole().getDisplayName().toUpperCase());
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRole.setForeground(UITheme.ACCENT_CYAN);

        userChip.add(lblName);
        userChip.add(Box.createVerticalStrut(2));
        userChip.add(lblRole);

        topSidebar.add(lblBrand);
        topSidebar.add(Box.createVerticalStrut(2));
        topSidebar.add(lblApp);
        topSidebar.add(Box.createVerticalStrut(16));
        topSidebar.add(userChip);

        sidebarPanel.add(topSidebar, BorderLayout.NORTH);

        // Navigation Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        // Dynamic navigation depending on Role
        if (currentUser.getRole() == Role.ADMIN) {
            addNavItem(menuPanel, "DASHBOARD", "Executive Dashboard", new AdminDashboardPanel());
            addNavItem(menuPanel, "STUDENTS", "Student Directory", new StudentManagementPanel(currentUser));
            addNavItem(menuPanel, "SUBJECTS", "Courses & Syllabus", new SubjectManagementPanel(currentUser));
            addNavItem(menuPanel, "FACULTY", "Faculty Staff", new FacultyManagementPanel(currentUser));
            addNavItem(menuPanel, "MARKS", "Marks Evaluation", new MarksEntryPanel(currentUser));
            addNavItem(menuPanel, "ANALYTICS", "Analytics & Rankings", new AnalyticsPanel(currentUser));
            addNavItem(menuPanel, "AUDIT", "Audit Trail", new AuditLogsPanel());
        } else if (currentUser.getRole() == Role.FACULTY) {
            addNavItem(menuPanel, "DASHBOARD", "Faculty Workspace", new FacultyDashboardPanel(currentUser));
            addNavItem(menuPanel, "MARKS", "Marks Entry & Grading", new MarksEntryPanel(currentUser));
            addNavItem(menuPanel, "PERFORMANCE", "Class Performance", new ClassPerformancePanel(currentUser));
        } else if (currentUser.getRole() == Role.STUDENT) {
            addNavItem(menuPanel, "DASHBOARD", "My Grade Report", new StudentDashboardPanel(currentUser));
            addNavItem(menuPanel, "TRENDS", "Performance Trends", new PerformanceTrendsPanel(currentUser));
        }

        sidebarPanel.add(menuPanel, BorderLayout.CENTER);

        // Sidebar Footer with Logout & DB Config
        JPanel bottomSidebar = new JPanel();
        bottomSidebar.setLayout(new BoxLayout(bottomSidebar, BoxLayout.Y_AXIS));
        bottomSidebar.setOpaque(false);
        bottomSidebar.setBorder(new EmptyBorder(12, 12, 20, 12));

        if (currentUser.getRole() == Role.ADMIN) {
            JButton btnDb = createSidebarButton("MySQL Settings...");
            btnDb.addActionListener(e -> {
                DbConfigDialog dlg = new DbConfigDialog(this);
                dlg.setVisible(true);
            });
            bottomSidebar.add(btnDb);
            bottomSidebar.add(Box.createVerticalStrut(6));
        }

        JButton btnLogout = createSidebarButton("Sign Out");
        btnLogout.addActionListener(e -> performLogout());
        bottomSidebar.add(btnLogout);

        sidebarPanel.add(bottomSidebar, BorderLayout.SOUTH);
        add(sidebarPanel, BorderLayout.WEST);

        // 2. Center Content Container (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setOpaque(false);

        for (Map.Entry<String, BasePanel> entry : panels.entrySet()) {
            contentContainer.add(entry.getValue(), entry.getKey());
        }

        add(contentContainer, BorderLayout.CENTER);

        // Show default first panel
        if (!panels.isEmpty()) {
            String firstKey = panels.keySet().iterator().next();
            switchPanel(firstKey);
        }
    }

    private void addNavItem(JPanel menuPanel, String key, String text, BasePanel panel) {
        panels.put(key, panel);

        JButton btn = createSidebarButton(text);
        btn.addActionListener(e -> switchPanel(key));
        navButtons.put(key, btn);

        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(4));
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UITheme.NAVY_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UITheme.PRIMARY_BLUE)) {
                    btn.setBackground(UITheme.NAVY_SURFACE);
                    btn.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UITheme.PRIMARY_BLUE)) {
                    btn.setBackground(UITheme.NAVY_DARK);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });
        return btn;
    }

    private void switchPanel(String key) {
        activeNavKey = key;
        cardLayout.show(contentContainer, key);

        // Highlight active nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(key)) {
                b.setBackground(UITheme.PRIMARY_BLUE);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(UITheme.NAVY_DARK);
                b.setForeground(new Color(203, 213, 225));
            }
        }

        // Refresh panel data
        BasePanel p = panels.get(key);
        if (p != null) {
            p.refreshData();
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of your session?",
                "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Exit the Student Performance & Grade Management System?",
                "Exit Application", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseConfig.shutdown();
            System.exit(0);
        }
    }
}
