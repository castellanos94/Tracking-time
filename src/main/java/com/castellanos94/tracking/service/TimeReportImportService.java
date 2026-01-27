package com.castellanos94.tracking.service;

import com.castellanos94.tracking.db.CategoryDAO;
import com.castellanos94.tracking.db.TimeEntryDAO;
import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.FormatExportEnum;
import com.castellanos94.tracking.model.TimeEntry;
import com.castellanos94.tracking.model.TimeReport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TimeReportImportService {

    private final TimeEntryDAO timeEntryDAO;
    private final CategoryDAO categoryDAO;

    public TimeReportImportService() {
        this.timeEntryDAO = new TimeEntryDAO();
        this.categoryDAO = new CategoryDAO();
    }

    public void importData(File file, FormatExportEnum format, Category defaultCategory)
            throws IOException, SQLException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }

        List<TimeEntry> entries = new ArrayList<>();

        switch (format) {
            case JSON:
                entries = parseJSON(file);
                break;
            case XLSX:
                entries = parseXLSX(file);
                break;
            case CSV:
                entries = parseCSV(file);
                break;
            case CUSTOM:
                if (defaultCategory == null)
                    throw new IllegalArgumentException("Default Category is required for Custom Import");
                entries = parseCustom(file, defaultCategory);
                break;
        }

        log.info("Parsed {} entries. Saving to database...", entries.size());
        for (TimeEntry entry : entries) {
            try {
                // Ensure category exists or use default/create logic if needed.
                // For now assuming ID is present in JSON, or name in others.
                // Re-mapping logic might be complex, keeping simple for this iteration.
                if (entry.getId() == null) {
                    entry.setId(UUID.randomUUID().toString());
                }
                timeEntryDAO.save(entry);
            } catch (SQLException e) {
                log.error("Failed to save entry: " + entry, e);
            }
        }
    }

    private List<TimeEntry> parseJSON(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Assuming JSON export structure matches TimeReport list, but mapping back to
        // TimeEntry.
        // If export was TimeReport (DTO), we need to map to TimeEntry (Entity).
        // Let's assume for now implementation plan meant importing previously exported
        // data,
        // which might be TimeReport format, so we need adaptation.
        List<TimeReport> reports = mapper.readValue(file, new TypeReference<List<TimeReport>>() {
        });
        return mapReportsToEntries(reports, null);
    }

    private List<TimeEntry> parseXLSX(File file) throws IOException {
        List<TimeReport> reports = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header
                TimeReport r = new TimeReport();
                r.setCategory(getMessage(row, 0));
                r.setDescription(getMessage(row, 1));
                r.setStart(getMessage(row, 2));
                r.setEnd(getMessage(row, 3));
                // other fields ignore for now
                reports.add(r);
            }
        }
        return mapReportsToEntries(reports, null);
    }

    private List<TimeEntry> parseCSV(File file) throws IOException {
        List<TimeReport> reports = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    TimeReport r = new TimeReport();
                    r.setCategory(parts[0]);
                    r.setDescription(parts[1]);
                    r.setStart(parts[2]);
                    r.setEnd(parts[3]);
                    reports.add(r);
                }
            }
        }
        return mapReportsToEntries(reports, null);
    }

    private List<TimeEntry> parseCustom(File file, Category defaultCategory) throws IOException {
        // Custom Format: Date, Description, Time (Hours), Payment
        List<TimeEntry> entries = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            String line;
            // Assuming no header or maybe first line check? Let's check if first line looks
            // like header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4)
                    continue;

                // Simple heuristic to skip header
                if (parts[0].equalsIgnoreCase("date") || parts[0].equalsIgnoreCase("fecha"))
                    continue;

                try {
                    String dateStr = parts[0].trim();
                    String desc = parts[1].trim();
                    double hours = Double.parseDouble(parts[2].trim());
                    double payment = Double.parseDouble(parts[3].trim());

                    TimeEntry entry = new TimeEntry();
                    entry.setId(UUID.randomUUID().toString());
                    entry.setCategoryId(defaultCategory.getId());
                    entry.setDescription(desc);

                    // Calculate Rate based on payment and hours
                    if (hours > 0) {
                        entry.setHourlyRate(payment / hours);
                    } else {
                        entry.setHourlyRate(0.0);
                    }

                    // Mock Start/End times
                    LocalDate date = LocalDate.parse(dateStr); // Assuming ISO format for simplicity, user might need
                                                               // formatter
                    LocalDateTime start = date.atTime(9, 0); // Start at 9 AM
                    LocalDateTime end = start.plusMinutes((long) (hours * 60));

                    entry.setStartTime(start);
                    entry.setEndTime(end);

                    entries.add(entry);

                } catch (Exception e) {
                    log.warn("Skipping invalid line: " + line, e);
                }
            }
        }
        return entries;
    }

    private String getMessage(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null)
            return "";
        return row.getCell(cellIndex).toString();
    }

    private List<TimeEntry> mapReportsToEntries(List<TimeReport> reports, Category defaultCategory) {
        List<TimeEntry> entries = new ArrayList<>();
        // Pre-load categories to find IDs by Name
        List<Category> categories = new ArrayList<>();
        try {
            categories = categoryDAO.findAll();
        } catch (SQLException e) {
            log.error("Failed to load categories", e);
        }

        for (TimeReport r : reports) {
            TimeEntry t = new TimeEntry();
            t.setId(UUID.randomUUID().toString());
            t.setDescription(r.getDescription());
            t.setStartTime(LocalDateTime.parse(r.getStart()));
            if (r.getEnd() != null && !r.getEnd().isEmpty()) {
                t.setEndTime(LocalDateTime.parse(r.getEnd()));
            }

            // Find category ID
            String catName = r.getCategory();
            String catId = null;
            for (Category c : categories) {
                if (c.getName().equalsIgnoreCase(catName)) {
                    catId = c.getId();
                    t.setHourlyRate(c.getHourlyRate()); // Default to current rate if not present
                    break;
                }
            }
            if (catId == null && defaultCategory != null) {
                catId = defaultCategory.getId();
                t.setHourlyRate(defaultCategory.getHourlyRate());
            }

            if (catId != null) {
                t.setCategoryId(catId);
                entries.add(t);
            } else {
                log.warn("Skipping entry with unknown category: " + catName);
            }
        }
        return entries;
    }
}
