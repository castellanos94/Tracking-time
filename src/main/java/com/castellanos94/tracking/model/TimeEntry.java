package com.castellanos94.tracking.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;

public class TimeEntry {
    private String categoryId;
    private double hourlyRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    public TimeEntry() {
    }

    public TimeEntry(String categoryId, LocalDateTime startTime) {
        this(categoryId, startTime, 0.0);
    }

    public TimeEntry(String categoryId, LocalDateTime startTime, double hourlyRate) {
        this.categoryId = categoryId;
        this.startTime = startTime;
        this.hourlyRate = hourlyRate;
    }

    public void stop(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @JsonIgnore(value = true)
    public long getDurationSeconds() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now()).getSeconds();
        }
        return Duration.between(startTime, endTime).getSeconds();
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
