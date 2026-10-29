package com.university.grademanagement.ui;

import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Interactive Database Configuration dialog allowing administrators to set MySQL credentials,
 * test connectivity, and initialize database schemas with sample seed data.
 */
public class DbConfigDialog extends JDialog {

    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDatabase;
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JLabel lblFeedback;

    private boolean configured = false;

    public DbConfigDialog(Frame owner) {
        super(owner, "Database Connection Settings", true);
        setSize(460, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_MAIN);

        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.NAVY_DARK);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("MySQL Database Configuration");
        title.setFont(UITheme.FONT_HEADER_MEDIUM);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Configure your local MySQL Server 8 connection parameters");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        txtHost = UITheme.createTextField(20);
        txtHost.setText(DatabaseConfig.getHost());

        txtPort = UITheme.createTextField(10);
        txtPort.setText(String.valueOf(DatabaseConfig.getPort()));

        txtDatabase = UITheme.createTextField(20);
        txtDatabase.setText(DatabaseConfig.getDatabase());

        txtUser = UITheme.createTextField(20);
        txtUser.setText(DatabaseConfig.getUser());

        txtPassword = UITheme.createPasswordField(20);
        txtPassword.setText(DatabaseConfig.getPassword());

        addFormField(form, gbc, 0, "MySQL Host:", txtHost);
        addFormField(form, gbc, 1, "Port Number:", txtPort);
        addFormField(form, gbc, 2, "Database Name:", txtDatabase);
        addFormField(form, gbc, 3, "Username:", txtUser);
        addFormField(form, gbc, 4, "Password:", txtPassword);

        lblFeedback = new JLabel(" ");
        lblFeedback.setFont(UITheme.FONT_SMALL);
        lblFeedback.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(lblFeedback, gbc);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.CARD_BORDER));

        JButton btnTest = UITheme.createSecondaryButton("Test Connection");
        JButton btnInit = UITheme.createSecondaryButton("Create DB & Seed Data");
        JButton btnSave = UITheme.createPrimaryButton("Save & Connect");
        JButton btnCancel = UITheme.createSecondaryButton("Close");

        btnTest.addActionListener(e -> testConnection());
        btnInit.addActionListener(e -> initializeDatabase());
        btnSave.addActionListener(e -> saveAndConnect());
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnTest);
        footer.add(btnInit);
        footer.add(btnSave);
        footer.add(btnCancel);

        add(footer, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void testConnection() {
        lblFeedback.setText("Testing connection...");
        lblFeedback.setForeground(UITheme.TEXT_MUTED);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    int port = Integer.parseInt(txtPort.getText().trim());
                    return DatabaseConfig.testConnection(
                            txtHost.getText().trim(),
                            port,
                            txtDatabase.getText().trim(),
                            txtUser.getText().trim(),
                            new String(txtPassword.getPassword())
                    );
                } catch (Exception ex) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        lblFeedback.setText("Success! Connected to MySQL server.");
                        lblFeedback.setForeground(UITheme.SUCCESS_GREEN);
                    } else {
                        lblFeedback.setText("Connection failed. Check host, port, user or password.");
                        lblFeedback.setForeground(UITheme.DANGER_RED);
                    }
                } catch (Exception ex) {
                    lblFeedback.setText("Error testing connection: " + ex.getMessage());
                    lblFeedback.setForeground(UITheme.DANGER_RED);
                }
            }
        };
        worker.execute();
    }

    private void initializeDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will create the database schema and populate it with sample students, courses, and marks.\nContinue?",
                "Initialize Database", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        lblFeedback.setText("Initializing database...");
        lblFeedback.setForeground(UITheme.PRIMARY_BLUE);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                int port = Integer.parseInt(txtPort.getText().trim());
                DatabaseConfig.updateConfiguration(
                        txtHost.getText().trim(),
                        port,
                        txtDatabase.getText().trim(),
                        txtUser.getText().trim(),
                        new String(txtPassword.getPassword())
                );
                DatabaseConfig.initializeDatabaseIfRequired();
                return true;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblFeedback.setText("Database schema and seed data loaded successfully!");
                    lblFeedback.setForeground(UITheme.SUCCESS_GREEN);
                    JOptionPane.showMessageDialog(DbConfigDialog.this,
                            "Database initialized successfully!\nDemo credentials available:\nAdmin: admin / Admin@123\nFaculty: dr.sharma / Faculty@123\nStudent: cs2023001 / Student@123",
                            "Setup Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    lblFeedback.setText("Init failed: " + ex.getMessage());
                    lblFeedback.setForeground(UITheme.DANGER_RED);
                    JOptionPane.showMessageDialog(DbConfigDialog.this,
                            "Database initialization failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void saveAndConnect() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            boolean success = DatabaseConfig.updateConfiguration(
                    txtHost.getText().trim(),
                    port,
                    txtDatabase.getText().trim(),
                    txtUser.getText().trim(),
                    new String(txtPassword.getPassword())
            );

            if (success) {
                configured = true;
                dispose();
            } else {
                lblFeedback.setText("Could not establish connection with these credentials.");
                lblFeedback.setForeground(UITheme.DANGER_RED);
            }
        } catch (NumberFormatException e) {
            lblFeedback.setText("Port must be a valid numeric value.");
            lblFeedback.setForeground(UITheme.DANGER_RED);
        }
    }

    public boolean isConfigured() {
        return configured;
    }
}
