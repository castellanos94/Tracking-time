package com.castellanos94.tracking.service;

import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.DataWrapper;
import com.castellanos94.tracking.model.TimeEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TimerService {

    private static TimerService instance;
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<TimeEntry> history = FXCollections.observableArrayList();

    private TimeEntry currentEntry;
    private ObjectMapper mapper;
    private final File dataFile = new File(System.getProperty("user.home"), ".tracking-time-data.json");
    private java.util.List<TimeEntry> archivedHistory = new java.util.ArrayList<>();

    private TimerService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
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
    }

    public void stopTimer() {
        if (currentEntry != null) {
            currentEntry.stop(LocalDateTime.now());
            history.add(0, currentEntry); // Add to top
            saveData();
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
        categories.add(c);
        saveData();
    }

    public void saveData() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setCategories(new java.util.ArrayList<>(categories));

            // Combine current history (today) with archived history for saving
            java.util.List<TimeEntry> allHistory = new java.util.ArrayList<>(history);
            if (archivedHistory != null) {
                allHistory.addAll(archivedHistory);
            }
            wrapper.setHistory(allHistory);

            mapper.writeValue(dataFile, wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        if (dataFile.exists()) {
            try {

                DataWrapper wrapper = mapper.readValue(dataFile, DataWrapper.class);
                if (wrapper.getCategories() != null) {
                    categories.setAll(wrapper.getCategories());
                }
                if (wrapper.getHistory() != null) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    history.clear();
                    archivedHistory.clear();
                    for (TimeEntry entry : wrapper.getHistory()) {
                        if (entry.getStartTime().toLocalDate().equals(today)) {
                            history.add(entry);
                        } else {
                            archivedHistory.add(entry);
                        }
                    }
                    // Sort history by start time desc
                    history.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Default data if empty (only if no categories loaded)
        if (categories.isEmpty()) {
            categories.add(new Category("Development", "#4CAF50", 50.0));
            categories.add(new Category("Meetings", "#2196F3", 30.0));
            categories.add(new Category("Research", "#FFC107", 40.0));
        }
    }

}
