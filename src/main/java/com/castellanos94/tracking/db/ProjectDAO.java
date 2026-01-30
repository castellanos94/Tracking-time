package com.castellanos94.tracking.db;

import com.castellanos94.tracking.model.Project;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProjectDAO {

    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                projects.add(map(rs));
            }
        } catch (SQLException e) {
            log.error("Error fetching projects", e);
        }
        return projects;
    }

    public void save(Project project) {
        String sql = "INSERT INTO projects (id, name, description, status, owner) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, project.getId());
            pstmt.setString(2, project.getName());
            pstmt.setString(3, project.getDescription());
            pstmt.setString(4, project.getStatus().name());
            pstmt.setString(5, project.getOwner());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error saving project", e);
        }
    }

    public void update(Project project) {
        String sql = "UPDATE projects SET name = ?, description = ?, status = ?, owner = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, project.getStatus().name());
            pstmt.setString(4, project.getOwner());
            pstmt.setString(5, project.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating project", e);
        }
    }

    public void delete(String id) {
        // Soft delete
        String sql = "UPDATE projects SET status = 'ARCHIVED' WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting project", e);
        }
    }

    public Optional<Project> findById(String id) {
        if (id == null)
            return Optional.empty();
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding project by id", e);
        }
        return Optional.empty();
    }

    private Project map(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setStatus(Project.ProjectStatus.valueOf(rs.getString("status")));
        p.setOwner(rs.getString("owner"));
        return p;
    }

    public List<Project> getAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                projects.add(map(rs));
            }
        } catch (SQLException e) {
            log.error("Error fetching projects", e);
        }
        return projects;
    }
}
