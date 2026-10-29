package com.university.grademanagement.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer drawing rounded pill badges for academic statuses, grades, and flags.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    public StatusBadgeRenderer() {
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value == null) {
            label.setText("");
            return label;
        }

        String text = value.toString().trim();
        label.setText(text);
        label.setFont(UITheme.FONT_BOLD);

        Color bgColor = Color.WHITE;
        Color fgColor = UITheme.TEXT_PRIMARY;

        if (text.equalsIgnoreCase("PASSED") || text.equalsIgnoreCase("PASS") || text.equalsIgnoreCase("Active") || text.equalsIgnoreCase("A+") || text.equalsIgnoreCase("A")) {
            bgColor = UITheme.SUCCESS_BG;
            fgColor = new Color(21, 128, 61); // Darker emerald for text contrast
        } else if (text.startsWith("FAILED") || text.equalsIgnoreCase("FAIL") || text.equalsIgnoreCase("Inactive") || text.equalsIgnoreCase("Deactivated") || text.equalsIgnoreCase("F")) {
            bgColor = UITheme.DANGER_BG;
            fgColor = new Color(185, 28, 28); // Darker red
        } else if (text.contains("Backlog") || text.equalsIgnoreCase("INCOMPLETE") || text.contains("Pending") || text.contains("Low")) {
            bgColor = UITheme.WARNING_BG;
            fgColor = new Color(180, 83, 9); // Darker amber
        } else if (text.equalsIgnoreCase("B") || text.equalsIgnoreCase("C")) {
            bgColor = UITheme.INFO_BG;
            fgColor = new Color(29, 78, 216);
        } else if (text.equalsIgnoreCase("D") || text.equalsIgnoreCase("E")) {
            bgColor = new Color(241, 245, 249);
            fgColor = new Color(71, 85, 105);
        }

        final Color bg = bgColor;
        final Color fg = fgColor;

        JPanel badgePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isSelected) {
                    g.setColor(table.getSelectionBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.setColor(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                int padX = 8;
                int padY = 4;
                int width = getWidth() - (padX * 2);
                int height = getHeight() - (padY * 2);
                g2.fillRoundRect(padX, padY, width, height, 12, 12);
                g2.dispose();
            }
        };

        label.setOpaque(false);
        label.setForeground(fg);
        badgePanel.add(label);
        return badgePanel;
    }
}
