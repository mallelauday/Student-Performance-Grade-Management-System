package com.university.grademanagement;

import com.formdev.flatlaf.FlatLightLaf;
import com.university.grademanagement.config.DatabaseConfig;
import com.university.grademanagement.ui.LoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Application Entry Point.
 * Configures the modern FlatLaf desktop theme, initializes database connections,
 * and launches the authentication interface on the Swing Event Dispatch Thread (EDT).
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Enable high-DPI scaling and text anti-aliasing
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize FlatLaf Modern Look & Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.thumbArc", 6);
            UIManager.put("ScrollBar.width", 10);
            logger.info("FlatLaf Modern Look & Feel initialized successfully.");
        } catch (Exception e) {
            logger.warn("Could not set FlatLaf Look & Feel, falling back to system theme: {}", e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Database asynchronously
        new Thread(() -> {
            try {
                if (DatabaseConfig.isConnected()) {
                    DatabaseConfig.initializeDatabaseIfRequired();
                    logger.info("Database initialized and verified ready.");
                } else {
                    logger.warn("MySQL server not reachable with current credentials. User can configure via GUI.");
                }
            } catch (Exception e) {
                logger.warn("Database initialization notice: {}", e.getMessage());
            }
        }).start();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            loginFrame.toFront();
            loginFrame.requestFocus();
            System.out.println("============================================================================");
            System.out.println("Student Performance & Grade Management System launched successfully!");
            System.out.println("Login window is now active on your desktop.");
            System.out.println("============================================================================");
        });
    }
}
