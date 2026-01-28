package com.castellanos94.tracking.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.castellanos94.tracking.db.CategoryDAO;
import com.castellanos94.tracking.db.TimeEntryDAO;
import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.TimeEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeEntryService {
    private final TimeEntryDAO timeEntryDAO;
    private final CategoryDAO categoryDAO;

    public TimeEntryService() {
        this.timeEntryDAO = new TimeEntryDAO();
        this.categoryDAO = new CategoryDAO();
    }

    public List<TimeEntry> getEntriesBetweenDates(LocalDate start, LocalDate end) {
        try {
            List<TimeEntry> byRange = timeEntryDAO.findByRange(start.atStartOfDay(), end.atTime(23, 59, 59));
            for (TimeEntry timeEntry : byRange) {
                Category category = categoryDAO.getById(timeEntry.getCategoryId());
                timeEntry.setCategoryName(category.getName());
            }
            return byRange;
        } catch (SQLException e) {
            log.error("Error fetching entries between dates", e);
            return new ArrayList<>();
        }
    }

    public boolean update(TimeEntry entry) {
        try {
            return timeEntryDAO.update(entry) > 0;
        } catch (SQLException e) {
            log.error("Error updating entry", e);
            return false;
        }
    }

    public boolean delete(TimeEntry entry) {
        try {
            return timeEntryDAO.delete(entry) > 0;
        } catch (SQLException e) {
            log.error("Errsor deleting entry", e);
            return false;
        }
    }
}
