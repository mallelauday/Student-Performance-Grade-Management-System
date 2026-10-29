package com.university.grademanagement.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Polished JTable configured with consistent typography, zebra striping, and custom headers.
 */
public class StyledTable extends JTable {

    public StyledTable(TableModel model) {
        super(model);
        initStyle();
    }

    public StyledTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(34);
        setFont(UITheme.FONT_REGULAR);
        setShowGrid(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionBackground(new Color(239, 246, 255));
        setSelectionForeground(UITheme.TEXT_PRIMARY);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);

        // Header style
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(100, 38));
        header.setFont(UITheme.FONT_BOLD);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(UITheme.NAVY_DARK);
                l.setForeground(Color.WHITE);
                l.setFont(UITheme.FONT_BOLD);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                l.setHorizontalAlignment(CENTER);
                return l;
            }
        });

        // Alternating zebra striping renderer for default cells
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    public static JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }
}
