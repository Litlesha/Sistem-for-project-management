package org.diploma.fordiplom.entity.DTO.request;

import java.util.List;

public class CompleteSprintRequest {
    private Long projectId;
    private List<Long> taskIds;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }
}
