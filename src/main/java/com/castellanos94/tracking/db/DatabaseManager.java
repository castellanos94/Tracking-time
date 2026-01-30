package com.castellanos94.tracking.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DatabaseManager {

    private static final String DB_URL = "jdbc:derby:" + System.getProperty("user.home")
            + "/.tracking-time-db;create=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create Categories Table
            try {
                stmt.execute("""
                        CREATE TABLE categories (
                            id VARCHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            color VARCHAR(50),
                            hourly_rate DOUBLE
                        )
                        """);
                log.info("Table 'categories' created.");
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState())) { // X0Y32: Table already exists
                    throw e;
                }
            }

            // Create TimeEntries Table
            try {
                stmt.execute("""
                        CREATE TABLE time_entries (
                            id VARCHAR(36) PRIMARY KEY,
                            category_id VARCHAR(36),
                            start_time TIMESTAMP,
                            end_time TIMESTAMP,
                            description VARCHAR(1000),
                            hourly_rate DOUBLE,
                            FOREIGN KEY (category_id) REFERENCES categories(id)
                        )
                        """);
                log.info("Table 'time_entries' created.");
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState())) {
                    throw e;
                }
            }

        } catch (SQLException e) {
            log.error("Database initialization failed", e);
            throw new RuntimeException("Database initialization failed", e);
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create Projects Table
            try {
                stmt.execute("""
                        CREATE TABLE projects (
                            id VARCHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description VARCHAR(1000),
                            status VARCHAR(50) DEFAULT 'ACTIVE'
                        )
                        """);
                log.info("Table 'projects' created.");
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState())) {
                    throw e;
                }
            }

            // Alter TimeEntries Table to add project_id
            try {
                stmt.execute("ALTER TABLE time_entries ADD COLUMN project_id VARCHAR(36)");
                stmt.execute(
                        "ALTER TABLE time_entries ADD CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id)");
                log.info("Column 'project_id' added to 'time_entries'.");
            } catch (SQLException e) {
                // X0Y32: Column already exists equivalent or similar error checking might be
                // needed depending on Derby version
                // Derby throws specific error if column exists.
                // Ideally we query system tables to check if column exists, but for simple
                // migration try-catch works.
                if (!"X0Y32".equals(e.getSQLState()) && !"42X14".equals(e.getSQLState())) { // 42X14: Column already
                                                                                            // exists
                    // log.warn("Alter table failed (might already exist): " + e.getMessage());
                }
            }

            // Alter Projects Table to add owner
            try {
                stmt.execute("ALTER TABLE projects ADD COLUMN owner VARCHAR(255)");
                log.info("Column 'owner' added to 'projects'.");
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState()) && !"42X14".equals(e.getSQLState())) {
                    // log.warn("Alter table projects failed (might already exist): " +
                    // e.getMessage());
                }
            }
        } catch (SQLException e) {
            log.warn("Project DB Migration warning (safe to ignore if already applied): {}", e.getMessage());
        }
    }
}
