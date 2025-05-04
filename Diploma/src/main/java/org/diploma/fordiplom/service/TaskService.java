package org.diploma.fordiplom.service;


import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.TaskRequest;
import org.diploma.fordiplom.entity.TaskEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaskService {
    TaskEntity createTask(TaskRequest request);
    String keyGenerator(TaskRequest request);
    List<TaskEntity> getTasksBySprintId(Long sprintId);
    List<TaskEntity> getBackLogTasksByProjectId(Long projectId);
    void updateTaskLocation(Long taskId, Long sprintId);
    void updateStatus(Long taskId, String status);
    List<TaskEntity> searchTasksInSprint(String query, Long projectId, Long sprintId);
    TaskEntity getTaskById(Long taskId);
}
