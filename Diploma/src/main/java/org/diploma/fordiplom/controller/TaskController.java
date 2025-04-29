package org.diploma.fordiplom.controller;


import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.request.TaskLocationUpdateRequest;
import org.diploma.fordiplom.entity.DTO.request.TaskRequest;
import org.diploma.fordiplom.entity.DTO.request.TaskStatusUpdateRequest;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping(path = "/create_task", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TaskEntity createNewTask(@RequestBody TaskRequest task) {
        return taskService.createTask(task);
    }


    @GetMapping("/api/project/{projectId}/backlog/backlog_tasks")
    public List<TaskEntity> getBacklogTasks(@PathVariable Long projectId) {
        return taskService.getBackLogTasksByProjectId(projectId);
    }

//    @GetMapping("/backlog_tasks")
//    public List<TaskEntity> getBacklogTasks(@PathVariable Long projectId) {
//        return taskService.getBackLogTasksByProjectId(projectId);
//    }


    @GetMapping("/sprint_tasks/backlog/{sprintId}")
    public List<TaskDTO> getTasksBySprintId(@PathVariable Long sprintId) {
        List<TaskEntity> tasks = taskService.getTasksBySprintId(sprintId);

        return tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getSprint() != null ? task.getSprint().getId() : null,
                        task.getTaskKey(),
                        task.getTaskType()
                ))
                .collect(Collectors.toList());
    }
    @PostMapping("/update_task_location")
    public ResponseEntity<?> updateTaskLocation(@RequestBody TaskLocationUpdateRequest request) {
        try {
            taskService.updateTaskLocation(request.getTaskId(), request.getSprintId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении задачи");
        }

    }
    @PostMapping("/update_status")
    public ResponseEntity<?> updateStatus(@RequestBody TaskStatusUpdateRequest request) {
        taskService.updateStatus(request.getTaskId(), request.getStatus());
        return ResponseEntity.ok().build();
    }
}