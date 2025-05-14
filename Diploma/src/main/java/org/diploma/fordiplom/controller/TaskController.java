package org.diploma.fordiplom.controller;


import jakarta.validation.Valid;
import org.diploma.fordiplom.entity.DTO.ProjectDTO;
import org.diploma.fordiplom.entity.DTO.TagDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.*;
import org.diploma.fordiplom.entity.DTO.response.TaskResponseDTO;
import org.diploma.fordiplom.entity.TagEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.TaskService;
import org.diploma.fordiplom.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private TeamService teamService;
    @PostMapping(path = "/create_task", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TaskEntity createNewTask(@RequestBody TaskRequest task) {
        return taskService.createTask(task);
    }


    @GetMapping("/api/project/{projectId}/backlog/backlog_tasks")
    public List<TaskEntity> getBacklogTasks(@PathVariable Long projectId) {
        return taskService.getBackLogTasksByProjectId(projectId);
    }

    @GetMapping("/sprint_tasks/backlog/{sprintId}")
    public List<TaskDTO> getTasksBySprintId(@PathVariable Long sprintId) {
        List<TaskEntity> tasks = taskService.getTasksBySprintId(sprintId);

        return tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getSprint() != null ? task.getSprint().getId() : null,
                        task.getTaskKey(),
                        task.getTaskType(),
                        task.getStatus(),
                        task.getPosition()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/update_task_location")
    public ResponseEntity<?> updateTaskLocation(@RequestBody TaskLocationUpdateRequest request) {
        try {
            taskService.updateTaskLocation(request.getTaskId(), request.getSprintId(), request.getPosition());
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
    @PostMapping("/update_task_positions")
    public ResponseEntity<?> updateTaskPositions(@RequestBody List<TaskPositionUpdateRequest> updates) {
        taskService.updateTaskPositions(updates);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/sprint/{sprintId}/search")
    public ResponseEntity<List<TaskDTO>> searchTasksInSprint(
            @PathVariable Long sprintId,
            @RequestParam Long projectId,
            @RequestParam String query) {
        // Получаем список TaskDTO от сервиса
        List<TaskDTO> tasks = taskService.searchTasksInSprint(query, projectId, sprintId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/sprint/{sprintId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksBySprintIdBoard(@PathVariable Long sprintId) {
        List<TaskEntity> tasks = taskService.getTasksBySprintId(sprintId);
        if (tasks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getSprint() != null ? task.getSprint().getId() : null,
                        task.getStatus(),
                        task.getTaskKey(),
                        task.getTaskType(),
                        task.getPosition()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDTOs);
    }

    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskEntity task = taskService.getTaskById(id);
        return ResponseEntity.ok(new TaskDTO(task));
    }
    @PutMapping("/api/tasks/{id}/title")
    public ResponseEntity<TaskResponseDTO> updateTaskTitle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskTitleRequest request
    ) {
        TaskEntity updatedTask = taskService.updateTaskTitle(id, request.getTitle());
        return ResponseEntity.ok(new TaskResponseDTO(updatedTask));
    }
    @PutMapping("/api/tasks/{id}/description")
    public ResponseEntity<TaskResponseDTO> updateTaskDescription(@PathVariable Long id,
                                                                 @RequestBody EditDescriptionRequest request){
        TaskEntity updatedTask = taskService.updateTaskDescription(id, request.getDescription());
        return ResponseEntity.ok(new TaskResponseDTO(updatedTask));
    }
    @PutMapping("/api/tasks/{id}/priority")
    public ResponseEntity<TaskResponseDTO> updateTaskPriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskPriorityRequest request,
            Principal principal // для получения пользователя
    ) {
        // Обновляем приоритет задачи и сохраняем её в БД
        TaskEntity updatedTask = taskService.updateTaskPriority(id, request.getPriority(), principal.getName());

        return ResponseEntity.ok(new TaskResponseDTO(updatedTask));
    }
    @PostMapping("/api/tasks/{taskId}/tags")
    public ResponseEntity<TagDTO> addTag(@PathVariable Long taskId, @RequestBody TagDTO tagDTO) {
        TagDTO createdTag = taskService.addTagToTask(taskId, tagDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @DeleteMapping("/api/tasks/{taskId}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(@PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/tasks/{taskId}/tags")
    public ResponseEntity<List<TagDTO>> getTags(@PathVariable Long taskId) {
        List<TagDTO> tags = taskService.getTagsForTask(taskId);
        return ResponseEntity.ok(tags);
    }
    @GetMapping("/api/tags/search")
    public List<TagDTO> searchTags(@RequestParam String query) {
        return taskService.searchTags(query);
    }
    @GetMapping("/api/tasks/{taskId}/project")
    public ResponseEntity<ProjectDTO> getProjectByTaskId(@PathVariable Long taskId) {
        try {
            ProjectDTO project = projectService.getProjectByTaskId(taskId);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PutMapping("/api/tasks/{taskId}/team/{teamId}")
    public ResponseEntity<Void> assignTeamToTask(@PathVariable Long taskId, @PathVariable Long teamId) {
        taskService.assignTeam(taskId, teamId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/api/tasks/{taskId}/executor/{userId}")
    public ResponseEntity<?> assignExecutor(@PathVariable Long taskId, @PathVariable Long userId) {
        taskService.assignExecutor(taskId, userId);
        return ResponseEntity.ok().build();
    }

}