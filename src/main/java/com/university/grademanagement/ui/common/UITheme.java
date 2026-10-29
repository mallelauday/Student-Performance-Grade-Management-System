package com.university.grademanagement.ui.common;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern University Design System and Theme.
 * Provides curated colors (navy, slate, vibrant blue, emerald, amber, rose),
 * consistent typography, and pre-styled UI components.
 */
public class UITheme {

    // Color Palette
    public static final Color NAVY_DARK = new Color(15, 23, 42);         // #0F172A (Sidebar / Dark surfaces)
    public static final Color NAVY_SURFACE = new Color(30, 41, 59);      // #1E293B (Sidebar item hover)
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);     // #2563EB (Primary action brand)
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);    // #1D4ED8
    public static final Color ACCENT_CYAN = new Color(14, 165, 233);     // #0EA5E9
    public static final Color BG_MAIN = new Color(248, 250, 252);        // #F8FAFC (Main app background)
    public static final Color CARD_BG = Color.WHITE;                     // #FFFFFF (Card surfaces)
    public static final Color CARD_BORDER = new Color(226, 232, 240);    // #E2E8F0 (Subtle borders)
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);      // #0F172A (Dark high contrast text)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748B (Secondary / subtitle text)
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);     // #F1F5F9 (Text on dark navy)

    // Semantic Status Colors
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);   // #10B981 (Pass / Active)
    public static final Color SUCCESS_BG = new Color(236, 253, 245);      // #ECFDF5
    public static final Color WARNING_AMBER = new Color(245, 158, 11);   // #F59E0B (Incomplete / Pending)
    public static final Color WARNING_BG = new Color(254, 243, 199);     // #FEF3C7
    public static final Color DANGER_RED = new Color(239, 68, 68);       // #EF4444 (Fail / Inactive)
    public static final Color DANGER_BG = new Color(254, 242, 242);       // #FEF2F2
    public static final Color INFO_BLUE = new Color(59, 130, 246);       // #3B82F6
    public static final Color INFO_BG = new Color(239, 246, 255);        // #EFF6FF

    // Typography
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER_SMALL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC_VALUE = new Font("Segoe UI", Font.BOLD, 24);

    private UITheme() {
    }

    /**
     * Creates a styled primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_BLUE);
            }
        });
        return btn;
    }

    /**
     * Creates a secondary bordered button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(241, 245, 249));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    /**
     * Creates a danger button (for deletions or deactivations).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER_RED);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(220, 38, 38));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DANGER_RED);
            }
        });
        return btn;
    }

    /**
     * Creates a styled text field.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setBackground(Color.WHITE);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    /**
     * Creates a styled card container.
     */
    public static JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return p;
    }
}
