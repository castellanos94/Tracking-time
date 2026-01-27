package com.castellanos94.tracking;

import com.castellanos94.tracking.db.DatabaseManager;
import com.castellanos94.tracking.db.TimeReportDAO;
import com.castellanos94.tracking.model.TimeReport;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class TimeReportTest {

    @BeforeAll
    public static void setup() {
        DatabaseManager.initialize();
    }

    @Test
    public void testFindAll() {
        TimeReportDAO dao = new TimeReportDAO();
        try {
            List<TimeReport> reports = dao.findAll();
            assertNotNull(reports, "Reports list should not be null");
            log.info("Reports found: " + reports.size());
            for (TimeReport report : reports) {
                log.info(report.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("SQLException should not occur: " + e.getMessage());
        }
    }

    @Test
    public void testFindByRange() {
        TimeReportDAO dao = new TimeReportDAO();
        try {
            List<TimeReport> reports = dao.findAll(java.time.LocalDateTime.now().minusDays(30),
                    java.time.LocalDateTime.now());
            assertNotNull(reports, "Reports list should not be null");
            log.info("Reports found: " + reports.size());
            for (TimeReport report : reports) {
                log.info(report.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("SQLException should not occur: " + e.getMessage());
        }
    }
}
