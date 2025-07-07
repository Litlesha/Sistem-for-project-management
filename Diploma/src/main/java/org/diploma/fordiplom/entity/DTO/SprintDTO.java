package org.diploma.fordiplom.entity.DTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class SprintDTO {
    private Long id;
    private String sprintName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String goal;
    private Integer duration;
    private Boolean isActive;
    private List<TaskDTO> tasks;


    public SprintDTO(Long id, String sprintName) {
        this.id = id;
        this.sprintName = sprintName;
    }

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
        this.id = sprint.getId();
        this.sprintName = sprint.getSprintName();
        this.startDate = sprint.getStartDate();
        this.endDate = sprint.getEndDate();
        this.goal = sprint.getGoal();
        this.duration = sprint.getDuration();
        this.isActive = sprint.getIsActive();
    }

}