package org.diploma.fordiplom.entity.DTO.request;

public class TaskLocationUpdateRequest {
    private Long taskId;
    private Long sprintId;

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }
}
