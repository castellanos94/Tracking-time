package com.castellanos94.tracking.service;

import java.sql.SQLException;
import java.util.List;

import com.castellanos94.tracking.db.CategoryDAO;
import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.TimeEntry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() {
        try {
            return categoryDAO.findAll();
        } catch (SQLException e) {
            log.error("Error getting all categories", e);
            return List.of();
        }
    }

    public List<String> getAllCategoriesNames() {
        try {
            return categoryDAO.findAll().stream().map(Category::getName).toList();
        } catch (SQLException e) {
            log.error("Error getting all categories", e);
            return List.of();
        }
    }

    public Category findCategoryByName(String newValue) {
        try {
            return categoryDAO.findByName(newValue);
        } catch (SQLException e) {
            log.error("Error getting category by name", e);
            return null;
        }
    }
}
