package com.castellanos94.tracking.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

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
    }
}
