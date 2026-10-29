package com.university.grademanagement.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Base panel providing standard layout, header controls, and status notifications.
 */
public abstract class BasePanel extends JPanel {

    protected JPanel headerPanel;
    protected JLabel lblTitle;
    protected JLabel lblSubtitle;
    protected JPanel actionsPanel;
    protected JPanel contentPanel;
    protected JLabel lblStatus;

    public BasePanel(String title, String subtitle) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_HEADER_LARGE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        titles.add(lblTitle);
        if (subtitle != null && !subtitle.isEmpty()) {
            titles.add(Box.createVerticalStrut(2));
            titles.add(lblSubtitle);
        }

        actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        headerPanel.add(titles, BorderLayout.WEST);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TEXT_MUTED);
        lblStatus.setBorder(new EmptyBorder(8, 4, 0, 4));
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void setStatus(String text) {
        lblStatus.setText(text);
    }

    public abstract void refreshData();
}
