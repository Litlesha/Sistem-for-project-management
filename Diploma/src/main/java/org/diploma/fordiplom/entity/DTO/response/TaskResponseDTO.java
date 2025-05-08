package org.diploma.fordiplom.entity.DTO.response;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.TaskEntity;

import java.time.Instant;
@Getter
@Setter
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String taskType;
    private String taskKey;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String authorName;
    private String authorEmail;
    private String boardName;
    private String sprintName;

    // конструктор
    public TaskResponseDTO(TaskEntity task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.taskType = task.getTaskType();
        this.taskKey = task.getTaskKey();
        this.status = task.getStatus();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();

        if (task.getAssignedUser() != null) {
            this.authorName = task.getAssignedUser().getUsername();
            this.authorEmail = task.getAssignedUser().getEmail();
        } else {
            this.authorName = "Не назначено";
            this.authorEmail = "Не назначено";
        }
        this.boardName = task.getProject() != null ? task.getProject().getName() : "Без доски";
        this.sprintName = task.getSprint() != null ? task.getSprint().getSprintName() : "Без спринта";
    }
}
