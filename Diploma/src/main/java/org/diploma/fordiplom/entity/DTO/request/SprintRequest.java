package org.diploma.fordiplom.entity.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SprintRequest {
    private String sprintName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String goal;
    private Integer duration;
    private Long projectId;
    private boolean isActive;

    public SprintRequest(Long id, @Size(max = 100) @NotNull String sprintName, Boolean isActive) {
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getSprintName() {return sprintName;}

    public void setSprintName(String sprintName) {this.sprintName = sprintName;}

    public @NotNull LocalDateTime getStartDate() {return startDate;}

    public void setStartDate(LocalDateTime startDate) {this.startDate = startDate;}

    public @NotNull LocalDateTime getEndDate() {return endDate;}

    public void setEndDate(@NotNull LocalDateTime endDate) {this.endDate = endDate;}

    public String getGoal() {return goal;}

    public void setGoal(String goal) {this.goal = goal;}

    public Integer getDuration() {return duration;}

    public void setDuration(Integer duration) {this.duration = duration;}
}
