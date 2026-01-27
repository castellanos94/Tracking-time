package com.castellanos94.tracking.db;

import com.castellanos94.tracking.model.Category;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CategoryDAO {

    public void save(Category category) throws SQLException {
        String sql = "INSERT INTO categories (id, name, color, hourly_rate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (category.getId() == null) {
                category.setId(UUID.randomUUID().toString());
            }
            // Check if exists/update or simple insert? For now simple insert, but we might
            // want upsert
            // Actually, let's check existence first
            if (exists(category.getId())) {
                update(category);
            } else {
                pstmt.setString(1, category.getId());
                pstmt.setString(2, category.getName());
                pstmt.setString(3, category.getColor());
                pstmt.setDouble(4, category.getHourlyRate());
                pstmt.executeUpdate();
            }
        }
    }

    public void update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, color = ?, hourly_rate = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getColor());
            pstmt.setDouble(3, category.getHourlyRate());
            pstmt.setString(4, category.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(Category category) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getId());
            pstmt.executeUpdate();
        }
    }

    public boolean exists(String id) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, color, hourly_rate FROM categories";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getString("id"));
                c.setName(rs.getString("name"));
                c.setColor(rs.getString("color"));
                c.setHourlyRate(rs.getDouble("hourly_rate"));
                list.add(c);
            }
        }
        return list;
    }
}
