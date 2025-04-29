package org.diploma.fordiplom.entity.DTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.diploma.fordiplom.entity.SprintEntity;

import java.time.LocalDateTime;

public class SprintDTO{
private Long id;
private String sprintName;
private LocalDateTime startDate;
private LocalDateTime endDate;
private String goal;
private Integer duration;
private Boolean isActive;

    public SprintDTO(Long id, String sprintName, LocalDateTime startDate, LocalDateTime endDate, String goal, Integer duration, Boolean isActive) {
        this.id = id;
        this.sprintName = sprintName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goal = goal;
        this.duration = duration;
        this.isActive = isActive;
    }

    public SprintDTO(SprintEntity sprint) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
