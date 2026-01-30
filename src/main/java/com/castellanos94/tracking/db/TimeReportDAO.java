package com.castellanos94.tracking.db;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.tracking.model.TimeReport;

import java.sql.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeReportDAO {
    public List<TimeReport> findAll() throws SQLException {
        List<TimeReport> list = new ArrayList<>();
        String sql = "SELECT start_time, end_time, time_entries.description, time_entries.hourly_rate, COALESCE(categories.name, 'No Category') AS category_name, "
                + "COALESCE(projects.name, '') AS project_name, COALESCE(projects.owner, '') AS project_owner, "
                + "{fn TIMESTAMPDIFF(SQL_TSI_SECOND, start_time, end_time)} / 3600.0 AS hours, " +
                "({fn TIMESTAMPDIFF(SQL_TSI_SECOND, start_time, end_time)} / 3600.0) * time_entries.hourly_rate AS amount "
                + "FROM time_entries LEFT JOIN categories ON time_entries.category_id = categories.id "
                + "LEFT JOIN projects ON time_entries.project_id = projects.id "
                + "ORDER BY start_time";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeReport entry = mapRow(rs);
                    list.add(entry);
                }
            }
        }
        return list;
    }

    public List<TimeReport> findAll(java.time.LocalDateTime start, java.time.LocalDateTime end) throws SQLException {
        List<TimeReport> list = new ArrayList<>();
        String sql = "SELECT start_time, end_time, time_entries.description, time_entries.hourly_rate, COALESCE(categories.name, 'No Category') AS category_name, "
                + "COALESCE(projects.name, '') AS project_name, COALESCE(projects.owner, '') AS project_owner, "
                + "{fn TIMESTAMPDIFF(SQL_TSI_SECOND, start_time, end_time)} / 3600.0 AS hours, " +
                "({fn TIMESTAMPDIFF(SQL_TSI_SECOND, start_time, end_time)} / 3600.0) * time_entries.hourly_rate AS amount "
                + "FROM time_entries LEFT JOIN categories ON time_entries.category_id = categories.id "
                + "LEFT JOIN projects ON time_entries.project_id = projects.id "
                + "WHERE start_time >= ? AND end_time <= ? ORDER BY start_time";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeReport entry = mapRow(rs);
                    list.add(entry);
                }
            }
        }
        return list;
    }

    private TimeReport mapRow(ResultSet rs) {
        TimeReport report = new TimeReport();
        try {
            report.setCategory(rs.getString("category_name"));
            report.setProjectName(rs.getString("project_name"));
            report.setProjectOwner(rs.getString("project_owner"));
            report.setDescription(rs.getString("description"));
            report.setStart(rs.getTimestamp("start_time").toLocalDateTime().toString());
            report.setEnd(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime().toString()
                    : "");
            report.setHours(rs.getDouble("hours"));
            report.setRate(rs.getDouble("hourly_rate"));
            report.setAmount(rs.getDouble("amount"));
        } catch (SQLException e) {
            log.error("Error mapping row", e);
        }
        return report;
    }
}
