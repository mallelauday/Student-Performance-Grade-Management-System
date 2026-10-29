package com.university.grademanagement.config;

import com.university.grademanagement.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Thread-safe Database Configuration and HikariCP Connection Manager.
 * Loads connection settings from local properties or environment variables,
 * supports dynamic reconfiguration, and provides self-initializing database capabilities.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "db.properties";

    private static HikariDataSource dataSource;
    private static Properties dbProperties = new Properties();

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "student_grade_db";
    private static String user = "root";
    private static String password = "";

    static {
        loadConfiguration();
    }

    private DatabaseConfig() {
    }

    /**
     * Loads database credentials from db.properties or environment variables.
     */
    public static synchronized void loadConfiguration() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                dbProperties.load(in);
                host = dbProperties.getProperty("db.host", host);
                port = Integer.parseInt(dbProperties.getProperty("db.port", String.valueOf(port)));
                database = dbProperties.getProperty("db.name", database);
                user = dbProperties.getProperty("db.user", user);
                password = dbProperties.getProperty("db.password", password);
                logger.info("Loaded database configuration from {}", CONFIG_FILE);
            } catch (Exception e) {
                logger.warn("Could not load db.properties, using defaults: {}", e.getMessage());
            }
        } else {
            // Check environment variables as fallback
            if (System.getenv("DB_HOST") != null) host = System.getenv("DB_HOST");
            if (System.getenv("DB_PORT") != null) {
                try {
                    port = Integer.parseInt(System.getenv("DB_PORT"));
                } catch (NumberFormatException ignored) {}
            }
            if (System.getenv("DB_NAME") != null) database = System.getenv("DB_NAME");
            if (System.getenv("DB_USER") != null) user = System.getenv("DB_USER");
            if (System.getenv("DB_PASSWORD") != null) password = System.getenv("DB_PASSWORD");
        }
    }

    /**
     * Initializes the HikariCP connection pool.
     */
    public static synchronized boolean initializePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        try {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&characterEncoding=UTF-8",
                    host, port, database
            );
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);
            config.setPoolName("StudentGradePool");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP Connection Pool initialized successfully for database '{}'", database);
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP connection pool: {}", e.getMessage());
            dataSource = null;
            return false;
        }
    }

    /**
     * Obtains a connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            boolean success = initializePool();
            if (!success) {
                throw new SQLException("Database connection pool is not available. Please verify MySQL configuration.");
            }
        }
        return dataSource.getConnection();
    }

    /**
     * Tests connectivity with given parameters without modifying existing pool.
     */
    public static boolean testConnection(String testHost, int testPort, String testDb, String testUser, String testPass) {
        String url = String.format("jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=4000",
                testHost, testPort);
        try (Connection conn = DriverManager.getConnection(url, testUser, testPass)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.warn("Test connection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Tests the currently configured connection.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates configuration settings, saves to db.properties, and re-initializes pool.
     */
    public static synchronized boolean updateConfiguration(String newHost, int newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        user = newUser;
        password = newPass;

        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", database);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);

        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Student Performance and Grade Management System - Database Configuration");
            logger.info("Saved new database configuration to {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Could not write to db.properties: {}", e.getMessage());
        }

        return initializePool();
    }

    /**
     * Initializes the database schema and seeds initial data if required.
     */
    public static synchronized void initializeDatabaseIfRequired() {
        try {
            // First connect to MySQL server root to ensure DB exists
            String serverUrl = String.format("jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port);
            try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            // Now run schema script
            executeSqlFile("database/schema.sql");

            // Check if users table is populated; if empty, run seed data
            boolean needsSeed = false;
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    needsSeed = true;
                }
            }

            if (needsSeed) {
                logger.info("Database is empty. Populating with seed data...");
                executeSqlFile("database/seed_data.sql");
                logger.info("Seed data applied successfully!");
            }
        } catch (Exception e) {
            logger.error("Failed to execute database initialization: {}", e.getMessage());
            throw new DatabaseException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /**
     * Executes SQL statements from an external file or resource.
     */
    public static void executeSqlFile(String relativePath) throws IOException, SQLException {
        Path path = Paths.get(relativePath);
        String sql;
        if (Files.exists(path)) {
            sql = Files.readString(path, StandardCharsets.UTF_8);
        } else {
            // Try loading from classpath
            try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream(relativePath)) {
                if (is == null) {
                    throw new FileNotFoundException("Cannot find SQL file: " + relativePath);
                }
                sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // Split statements by semicolon while ignoring delimiters or comments
        String[] statements = sql.split(";(?=(?:[^'\"`]*['\"`][^'\"`]*['\"`])*[^'\"`]*$)");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String singleStmt : statements) {
                String trimmed = singleStmt.trim();
                // Skip empty lines, comments, and USE statements
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.toUpperCase().startsWith("USE ")) {
                    continue;
                }
                try {
                    stmt.execute(trimmed);
                } catch (SQLException e) {
                    logger.debug("Statement execution notice for [{}]: {}", trimmed.length() > 40 ? trimmed.substring(0, 40) + "..." : trimmed, e.getMessage());
                }
            }
        }
    }

    /**
     * Closes the connection pool on application shutdown.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }

    // Getters
    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
}
