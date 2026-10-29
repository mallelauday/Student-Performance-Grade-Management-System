package com.university.grademanagement.ui;

import com.university.grademanagement.config.AppConfig;
import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.model.User;
import com.university.grademanagement.service.AuthService;
import com.university.grademanagement.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern University Login Window with credential validation,
 * role routing, quick test profiles, and interactive database setup.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private JButton btnLogin;
    private JLabel lblDbStatus;

    public LoginFrame() {
        super(AppConfig.APP_NAME + " - Authentication");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // 1. Top University Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel lblBadge = new JLabel("UNIVERSITAS EXCELLENTIA");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(UITheme.ACCENT_CYAN);
        lblBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUnivName = new JLabel(AppConfig.UNIVERSITY_NAME);
        lblUnivName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUnivName.setForeground(Color.WHITE);
        lblUnivName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSystem = new JLabel(AppConfig.APP_NAME);
        lblSystem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSystem.setForeground(new Color(203, 213, 225));
        lblSystem.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblBadge);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(lblUnivName);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSystem);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Login Form Card
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(390, 380));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.CARD_BORDER, 1, true),
                new EmptyBorder(22, 24, 20, 24)
        ));

        JLabel lblLoginTitle = new JLabel("Account Sign In");
        lblLoginTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        lblLoginTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblLoginDesc = new JLabel("Enter your credentials to access your academic portal");
        lblLoginDesc.setFont(UITheme.FONT_SMALL);
        lblLoginDesc.setForeground(UITheme.TEXT_MUTED);

        card.add(lblLoginTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(lblLoginDesc);
        card.add(Box.createVerticalStrut(18));

        // Form fields
        JLabel lblUser = new JLabel("Username / Roll Number");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_PRIMARY);

        txtUsername = UITheme.createTextField(25);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_PRIMARY);

        txtPassword = UITheme.createPasswordField(25);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER_RED);

        btnLogin = UITheme.createPrimaryButton("Secure Sign In");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.addActionListener(e -> performLogin());

        // Enable Enter key submission
        txtUsername.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(12));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(8));
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));

        // Demo profile shortcuts for university examiners
        JLabel lblDemo = new JLabel("Quick Demo Profiles:");
        lblDemo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDemo.setForeground(UITheme.TEXT_MUTED);
        card.add(lblDemo);
        card.add(Box.createVerticalStrut(6));

        JPanel demoButtons = new JPanel(new GridLayout(1, 3, 6, 0));
        demoButtons.setOpaque(false);
        demoButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton btnAdmin = createQuickBtn("Admin", "admin", "Admin@123");
        JButton btnFaculty = createQuickBtn("Faculty", "dr.sharma", "Faculty@123");
        JButton btnStudent = createQuickBtn("Student", "cs2023001", "Student@123");

        demoButtons.add(btnAdmin);
        demoButtons.add(btnFaculty);
        demoButtons.add(btnStudent);
        card.add(demoButtons);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // 3. Bottom Database Status & Setup Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(241, 245, 249));
        bottomBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        lblDbStatus = new JLabel("Checking MySQL connection...");
        lblDbStatus.setFont(UITheme.FONT_SMALL);

        JButton btnDbConfig = new JButton("MySQL Settings...");
        btnDbConfig.setFont(UITheme.FONT_SMALL);
        btnDbConfig.setFocusPainted(false);
        btnDbConfig.setBorderPainted(false);
        btnDbConfig.setContentAreaFilled(false);
        btnDbConfig.setForeground(UITheme.PRIMARY_BLUE);
        btnDbConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDbConfig.addActionListener(e -> {
            DbConfigDialog dlg = new DbConfigDialog(this);
            dlg.setVisible(true);
            updateDbStatus();
        });

        bottomBar.add(lblDbStatus, BorderLayout.WEST);
        bottomBar.add(btnDbConfig, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);

        // Check DB in background
        updateDbStatus();
    }

    private JButton createQuickBtn(String title, String user, String pass) {
        JButton b = new JButton(title);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        b.setBackground(new Color(241, 245, 249));
        b.setForeground(UITheme.TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(UITheme.CARD_BORDER, 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            txtUsername.setText(user);
            txtPassword.setText(pass);
            lblError.setText(" ");
        });
        return b;
    }

    private void updateDbStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return DatabaseConfig.isConnected();
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        lblDbStatus.setText("● Connected to MySQL (" + DatabaseConfig.getDatabase() + ")");
                        lblDbStatus.setForeground(new Color(22, 163, 74));
                    } else {
                        lblDbStatus.setText("⚠ MySQL Not Connected (Click Settings)");
                        lblDbStatus.setForeground(UITheme.DANGER_RED);
                    }
                } catch (Exception ex) {
                    lblDbStatus.setText("⚠ Connection Error");
                    lblDbStatus.setForeground(UITheme.DANGER_RED);
                }
            }
        };
        worker.execute();
    }

    private void performLogin() {
        lblError.setText(" ");
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verifying...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Secure Sign In");
                try {
                    User user = get();
                    // Open main window
                    MainFrame mainFrame = new MainFrame(user);
                    mainFrame.setVisible(true);
                    LoginFrame.this.dispose();
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    lblError.setText(msg);
                }
            }
        };
        worker.execute();
    }
}
