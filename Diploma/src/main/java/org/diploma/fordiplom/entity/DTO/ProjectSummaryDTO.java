package org.diploma.fordiplom.entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
public class ProjectSummaryDTO {
    private int completedSprintsCount;
    private int completedTasksCount;
    private long projectDurationDays;
}
