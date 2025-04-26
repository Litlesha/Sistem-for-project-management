package org.diploma.fordiplom.entity.DTO.request;

import org.diploma.fordiplom.entity.UserEntity;

import java.time.Instant;

public class TaskRequest {
    private String title;
    private String description;
    private String priority;
    private String task_type;
    private String taskKey;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer assignedUser;
    private Long sprintId;
    private Long projectId;

    public Long getProjectId(){return projectId;}

    public void setProjectId(Long projectId){this.projectId = projectId;}

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTask_type() {
        return task_type;
    }

    public void setTask_type(String task_type) {
        this.task_type = task_type;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(Integer assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }
}
