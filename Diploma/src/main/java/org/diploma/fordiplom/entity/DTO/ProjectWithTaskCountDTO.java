package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.ProjectEntity;

@Getter
@Setter
public class ProjectWithTaskCountDTO {
    private Long id;
    private String name;
    private String key;
    private String description;
    private long openTasksCount;
    private long activeSprintsCount;

    public ProjectWithTaskCountDTO(ProjectEntity project, long openTasksCount, long activeSprintsCount) {
        this.id = project.getId();
        this.name = project.getName();
        this.key = project.getKey();
        this.description = project.getDescription();
        this.openTasksCount = openTasksCount;
        this.activeSprintsCount = activeSprintsCount;
    }
}
