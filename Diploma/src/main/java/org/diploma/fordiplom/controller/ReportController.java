package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.DTO.SprintDTO;
import org.diploma.fordiplom.entity.DTO.UserDTO;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.TaskService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")

public class ReportController {
    @Autowired
    private SprintService sprintService;
    @Autowired
    private UserService userService;
    @GetMapping("/filters/{projectId}")
    public ResponseEntity<Map<String, Object>> getSprintAndUserFilters(@PathVariable Long projectId) {
        List<SprintDTO> sprints = sprintService.getAllByProjectId(projectId).stream()
                .map(SprintDTO::new)
                .toList();

        List<UserDTO> users = userService.getUsersWithTasksByProjectId(projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("sprints", sprints);
        result.put("users", users);

        return ResponseEntity.ok(result);
    }
    @GetMapping("/user/{userId}/project/{projectId}")
    public ResponseEntity<UserDTO> getUserWithTasks(
            @PathVariable Long userId,
            @PathVariable Long projectId) {
        UserDTO userDTO = userService.getUserWithTasks(userId, projectId);
        return ResponseEntity.ok(userDTO);
    }
    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<SprintDTO> getSprintWithTasks(@PathVariable Long sprintId) {
        SprintDTO sprintDTO = sprintService.getSprintWithTasks(sprintId);
        return ResponseEntity.ok(sprintDTO);
    }
}
