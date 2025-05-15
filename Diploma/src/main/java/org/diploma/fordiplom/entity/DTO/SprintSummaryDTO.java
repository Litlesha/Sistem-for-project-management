package org.diploma.fordiplom.entity.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class SprintSummaryDTO {
        private int doneCount;
        private int openCount;
        private List<TaskDTO> openTasks;
    }

