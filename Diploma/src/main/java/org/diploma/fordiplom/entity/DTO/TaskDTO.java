package org.diploma.fordiplom.entity.DTO;

import org.diploma.fordiplom.entity.TaskEntity;

import java.security.Timestamp;

public class TaskDTO {
    private Long id;
    private String title;
    private Long sprintId;
    private String taskType;
    private String Status;
    private String taskKey;
    private String description;
    private String priority;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public TaskDTO(Long id, String title, Long sprintId, String taskType, String status, String taskKey, String description, String priority, Timestamp createdAt, Timestamp updatedAt) {
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public TaskDTO(Long id, String title, Long sprintId, String taskKey, String taskType, String status) {
        this.id = id;
        this.title = title;
        this.sprintId = sprintId;
        this.taskKey = taskKey;
        this.taskType = taskType;
        this.Status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
