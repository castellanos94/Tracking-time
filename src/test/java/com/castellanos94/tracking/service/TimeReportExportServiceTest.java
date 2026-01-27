package com.castellanos94.tracking.service;

import com.castellanos94.tracking.db.DatabaseManager;
import com.castellanos94.tracking.model.FormatExportEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TimeReportExportServiceTest {

    private static TimeReportExportService service;
    private Path tempFile;

    @BeforeAll
    public static void setup() {
        DatabaseManager.initialize();
        service = new TimeReportExportService();
    }

    @AfterEach
    public void cleanup() {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testExportJSON() {
        String filename = "test_export_json";
        try {
            service.export(null, null, FormatExportEnum.JSON, filename);
            tempFile = Paths.get(filename + ".json");
            assertTrue(Files.exists(tempFile), "JSON file should exist");
            assertTrue(Files.size(tempFile) > 0, "JSON file should not be empty");
        } catch (SQLException | IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    public void testExportCSV() {
        String filename = "test_export_csv";
        try {
            service.export(null, null, FormatExportEnum.CSV, filename);
            tempFile = Paths.get(filename + ".csv");
            assertTrue(Files.exists(tempFile), "CSV file should exist");
            assertTrue(Files.size(tempFile) > 0, "CSV file should not be empty");
        } catch (SQLException | IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    public void testExportXLSX() {
        String filename = "test_export_xlsx";
        try {
            service.export(null, null, FormatExportEnum.XLSX, filename);
            tempFile = Paths.get(filename + ".xlsx");
            assertTrue(Files.exists(tempFile), "XLSX file should exist");
            assertTrue(Files.size(tempFile) > 0, "XLSX file should not be empty");
        } catch (SQLException | IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }
}
