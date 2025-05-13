package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.TaskHistoryEntryDTO;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.TaskHistoryEntry;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TaskHistoryService {
    List<TaskHistoryEntryDTO> getHistoryForTask(Long taskId);
    TaskHistoryEntry saveTaskHistory(TaskEntity task, String oldValue, String newValue,
                                     String email, String actionCode, String fieldCode);
}
