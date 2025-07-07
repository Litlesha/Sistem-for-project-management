package org.diploma.fordiplom.entity.DTO.request;

import org.diploma.fordiplom.entity.TeamEntity;

public class RoleRequest {
    private Long teamId;
    private Long projectId;

    public RoleRequest(Long teamId, Long projectId) {
        this.teamId = teamId;
        this.projectId = projectId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
