package com.castellanos94.tracking.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String id;
    private String name;
    private String description;
    private String owner; // [NEW] Optional owner
    private ProjectStatus status;

    public Project(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = ProjectStatus.ACTIVE;
    }

    @Override
    public String toString() {
        if (owner != null && !owner.isEmpty()) {
            return name + " (" + owner + ")";
        }
        return name;
    }

    public enum ProjectStatus {
        ACTIVE, ARCHIVED
    }
}
