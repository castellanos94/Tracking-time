package com.castellanos94.tracking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper {
    private java.util.List<Category> categories;
    private java.util.List<TimeEntry> history;

    public DataWrapper() {
        this.categories = new java.util.ArrayList<>();
        this.history = new java.util.ArrayList<>();
    }

    public java.util.List<Category> getCategories() {
        return categories;
    }

    public void setCategories(java.util.List<Category> categories) {
        this.categories = categories;
    }

    public java.util.List<TimeEntry> getHistory() {
        return history;
    }

    public void setHistory(java.util.List<TimeEntry> history) {
        this.history = history;
    }
}
