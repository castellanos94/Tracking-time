package com.castellanos94.tracking.service;

import com.castellanos94.tracking.db.CategoryDAO;
import com.castellanos94.tracking.db.DatabaseManager;
import com.castellanos94.tracking.db.TimeEntryDAO;
import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.DataWrapper;
import com.castellanos94.tracking.model.TimeEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class TimerService {

    private static TimerService instance;
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<TimeEntry> history = FXCollections.observableArrayList();

    private TimeEntry currentEntry;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();

    // Legacy JSON support for migration
    private final File dataFile = new File(System.getProperty("user.home"), ".tracking-time-data.json");
    private ObjectMapper mapper;

    private TimerService() {
        DatabaseManager.initialize();
        loadData();
    }

    public static TimerService getInstance() {
        if (instance == null) {
            instance = new TimerService();
        }
        return instance;
    }

    public void startTimer(Category category, String description) {
        if (currentEntry != null) {
            stopTimer(); // specific behavior: auto-stop previous
        }
        if (category == null)
            return;

        currentEntry = new TimeEntry(category.getId(), LocalDateTime.now(), category.getHourlyRate());
        currentEntry.setDescription(description);
        try {
            timeEntryDAO.save(currentEntry);
        } catch (SQLException e) {
            log.error("Failed to start timer", e);
            throw new RuntimeException("Failed to start timer: " + e.getMessage(), e);
        }
    }

    public void stopTimer() {
        if (currentEntry != null) {
            currentEntry.stop(LocalDateTime.now());
            history.add(0, currentEntry); // Add to top
            try {
                timeEntryDAO.save(currentEntry);
            } catch (SQLException e) {
                log.error("Failed to stop timer", e);
                throw new RuntimeException("Failed to stop timer: " + e.getMessage(), e);
            }
            currentEntry = null;
        }
    }

    public boolean isRunning() {
        return currentEntry != null;
    }

    public TimeEntry getCurrentEntry() {
        return currentEntry;
    }

    public ObservableList<TimeEntry> getHistory() {
        return history;
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category c) {
        for (Category category : categories) {
            if (category.getName().equals(c.getName())) {
                log.warn("Category already exists: " + c.getName());
                throw new IllegalArgumentException("Category already exists: " + c.getName());
            }
        }
        try {
            categoryDAO.save(c);
            categories.add(c);
        } catch (SQLException e) {
            log.error("Failed to add category", e);
            throw new RuntimeException("Failed to add category: " + e.getMessage(), e);
        }
    }

    public void saveData() {
        // No-op for file save, DB saves immediately on actions
        // But maybe we want to update current entry?
        if (currentEntry != null) {
            try {
                timeEntryDAO.save(currentEntry);
            } catch (SQLException e) {
                log.error("Failed to save current entry", e);
            }
        }
    }

    private void loadData() {
        try {
            // 1. Load from DB
            categories.setAll(categoryDAO.findAll());
            history.setAll(timeEntryDAO.findAll());

            // 2. Migration Check
            if (categories.isEmpty() && history.isEmpty() && dataFile.exists()) {
                migrateLegacyData();
            }

            // 3. Defaults if still empty
            if (categories.isEmpty()) {
                addCategory(new Category("Development", "#4CAF50", 50.0));
                addCategory(new Category("Meetings", "#2196F3", 30.0));
                addCategory(new Category("Research", "#FFC107", 40.0));
            }

        } catch (SQLException e) {
            log.error("Failed to load data", e);
            throw new RuntimeException("Failed to load data: " + e.getMessage(), e);
        }
    }

    private void migrateLegacyData() {
        log.info("Migrating legacy data from JSON...");
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            DataWrapper wrapper = mapper.readValue(dataFile, DataWrapper.class);
            if (wrapper.getCategories() != null) {
                for (Category c : wrapper.getCategories()) {
                    try {
                        // Ensure ID
                        if (c.getId() == null)
                            c.setId(java.util.UUID.randomUUID().toString());
                        categoryDAO.save(c);
                        categories.add(c);
                    } catch (Exception ex) {
                        log.error("Failed to migrate category: " + c.getName(), ex);
                    }
                }
            }
            if (wrapper.getHistory() != null) {
                for (TimeEntry t : wrapper.getHistory()) {
                    try {
                        if (t.getId() == null)
                            t.setId(java.util.UUID.randomUUID().toString());
                        boolean catExists = categories.stream().anyMatch(cat -> cat.getId().equals(t.getCategoryId()));
                        if (!catExists) {
                            log.warn("Skipping time entry for unknown category: " + t.getCategoryId());
                            continue;
                        }

                        timeEntryDAO.save(t);
                        history.add(t);
                    } catch (Exception ex) {
                        log.error("Failed to migrate time entry", ex);
                    }
                }
            }

            // Rename legacy file to avoid re-migration
            File backup = new File(dataFile.getParent(), ".tracking-time-data.json.bak");
            if (dataFile.renameTo(backup)) {
                log.info("Legacy data migrated and file renamed to .bak");
            } else {
                log.warn("Failed to rename legacy data file");
            }

        } catch (Exception e) {
            log.error("Migration failed", e);
        }
    }
}
