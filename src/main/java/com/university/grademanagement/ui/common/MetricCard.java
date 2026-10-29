package com.university.grademanagement.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern dashboard metric card displaying an indicator bar, title, prominent value, and description.
 */
public class MetricCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private Color accentColor;

    public MetricCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : UITheme.PRIMARY_BLUE;
        setLayout(new BorderLayout());
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Top accent indicator bar
        JPanel topBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MetricCard.this.accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 2, 2);
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(100, 4));
        topBar.setOpaque(false);
        add(topBar, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_METRIC_VALUE);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblValue);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSubtitle);

        add(content, BorderLayout.CENTER);
    }

    public void update(String value, String subtitle) {
        lblValue.setText(value);
        if (subtitle != null) {
            lblSubtitle.setText(subtitle);
        }
    }

    public void setAccentColor(Color color) {
        this.accentColor = color;
        repaint();
    }
}
