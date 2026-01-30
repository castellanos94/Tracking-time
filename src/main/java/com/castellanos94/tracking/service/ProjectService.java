package com.castellanos94.tracking.service;

import java.util.List;

import com.castellanos94.tracking.db.ProjectDAO;
import com.castellanos94.tracking.model.Project;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectService {
    private final ProjectDAO projectDAO;

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public List<Project> getProjects() {
        List<Project> projects = projectDAO.getAll();
        projects.add(new Project("No Project"));
        return projects;
    }
}
