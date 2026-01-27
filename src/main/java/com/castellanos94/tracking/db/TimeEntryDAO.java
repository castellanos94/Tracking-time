package com.castellanos94.tracking.db;

import com.castellanos94.tracking.model.TimeEntry;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TimeEntryDAO {

    public void save(TimeEntry entry) throws SQLException {
        if (entry.getId() == null) {
            entry.setId(UUID.randomUUID().toString());
            create(entry);
        } else {
            update(entry);
        }
    }

    private void create(TimeEntry entry) throws SQLException {
        String sql = "INSERT INTO time_entries (id, category_id, start_time, end_time, description, hourly_rate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getId());
            pstmt.setString(2, entry.getCategoryId());
            pstmt.setTimestamp(3, Timestamp.valueOf(entry.getStartTime()));
            pstmt.setTimestamp(4, entry.getEndTime() != null ? Timestamp.valueOf(entry.getEndTime()) : null);
            pstmt.setString(5, entry.getDescription());
            pstmt.setDouble(6, entry.getHourlyRate());
            pstmt.executeUpdate();
        }
    }

    private void update(TimeEntry entry) throws SQLException {
        String sql = "UPDATE time_entries SET category_id = ?, start_time = ?, end_time = ?, description = ?, hourly_rate = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getCategoryId());
            pstmt.setTimestamp(2, Timestamp.valueOf(entry.getStartTime()));
            pstmt.setTimestamp(3, entry.getEndTime() != null ? Timestamp.valueOf(entry.getEndTime()) : null);
            pstmt.setString(4, entry.getDescription());
            pstmt.setDouble(5, entry.getHourlyRate());
            pstmt.setString(6, entry.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(TimeEntry entry) throws SQLException {
        String sql = "DELETE FROM time_entries WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getId());
            pstmt.executeUpdate();
        }
    }

    public List<TimeEntry> findAll() throws SQLException {
        List<TimeEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM time_entries ORDER BY start_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TimeEntry entry = mapRow(rs);
                list.add(entry);
            }
        }
        return list;
    }

    private TimeEntry mapRow(ResultSet rs) throws SQLException {
        TimeEntry entry = new TimeEntry();
        entry.setId(rs.getString("id"));
        entry.setCategoryId(rs.getString("category_id"));
        entry.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) {
            entry.setEndTime(endTs.toLocalDateTime());
        }
        entry.setDescription(rs.getString("description"));
        entry.setHourlyRate(rs.getDouble("hourly_rate"));
        return entry;
    }
}
