package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.TaskEntity;

import java.time.Instant;

import java.time.Instant;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private String title;
    private Long sprintId;
    private String taskType;
    private String Status;
    private String taskKey;
    private String description;
    private String priority;
    private Instant createdAt;
    private Instant updatedAt;
    private String authorName;
    private String boardName;
    private String sprintName;
    private TeamDTO team;
    private String executorName;
    public TaskDTO(Long id, String title, Long sprintId, String taskType, String status, String taskKey, String description, String priority, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.sprintId = sprintId;
        this.taskType = taskType;
        this.Status = status;
        this.taskKey = taskKey;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public TaskDTO(Long id, String title, Long sprintId, String taskKey, String taskType, String status) {
        this.id = id;
        this.title = title;
        this.sprintId = sprintId;
        this.taskKey = taskKey;
        this.taskType = taskType;
        this.Status = status;
    }

    public TaskDTO(TaskEntity taskEntity) {
        this.id = taskEntity.getId();
        this.title = taskEntity.getTitle();
        this.sprintId = taskEntity.getSprint() != null ? taskEntity.getSprint().getId() : null;
        this.taskType = taskEntity.getTaskType();
        this.Status = taskEntity.getStatus();
        this.taskKey = taskEntity.getTaskKey();
        this.description = taskEntity.getDescription();
        this.priority = taskEntity.getPriority();
        this.createdAt = taskEntity.getCreatedAt();
        this.updatedAt = taskEntity.getUpdatedAt();


        if (taskEntity.getAssignedUser() != null) {
            this.authorName = (taskEntity.getAssignedUser().getUsername() != null && !taskEntity.getAssignedUser().getUsername().isEmpty())
                    ? taskEntity.getAssignedUser().getUsername()
                    : taskEntity.getAssignedUser().getEmail();
        }
        this.sprintName = taskEntity.getSprint() != null ? taskEntity.getSprint().getSprintName() : null;
        this.team = taskEntity.getTeam() != null ? new TeamDTO(taskEntity.getTeam()) : null;
        if (taskEntity.getExecutor() != null) {
            this.executorName = taskEntity.getExecutor().getUsername() != null
                    ? taskEntity.getExecutor().getUsername()
                    : taskEntity.getExecutor().getEmail();
        }
    }
}
