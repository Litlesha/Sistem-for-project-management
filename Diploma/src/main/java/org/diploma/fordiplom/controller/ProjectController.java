package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.DTO.ProjectDTO;
import org.diploma.fordiplom.entity.DTO.TeamDTO;
import org.diploma.fordiplom.entity.DTO.request.AddTeamToProjectRequest;
import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @PostMapping(path = "/create_project", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectEntity createNewProject(@RequestBody ProjectRequest projectRequest, Principal principal) {
        return projectService.createProject(projectRequest, principal.getName());
    }
    @GetMapping("/api/project/{id}/backlog")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long id) {
        ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/api/project/{id}/board")
    public ResponseEntity<ProjectEntity> getProjectBoardById(@PathVariable Long id) {
        ProjectEntity project = projectService.getProjectById(id);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path="/api/projects")
    public List<ProjectEntity> getProjectsByUser(Principal principal) {
        String currentUserEmail = principal.getName(); // получаем email текущего пользователя
        UserEntity user = userService.getUserByEmail(currentUserEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return projectService.getProjectsByUserEmail(currentUserEmail);
    }
    @PostMapping("/api/projects/addTeamToProject")
    public ResponseEntity<String> addTeamToProject(@RequestBody AddTeamToProjectRequest request) {
        try {
            // Вызываем сервисный метод для добавления команды в проект
            projectService.addTeamToProject(request.getProjectId(), request.getTeamId());
            return ResponseEntity.ok("Команда успешно добавлена в проект");
        } catch (RuntimeException e) {
            // Обработка ошибок, если проект или команда не найдены
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Проект или команда не найдены");
        }
    }
    @GetMapping("/api/projects/{projectId}/teams")
    public ResponseEntity<List<TeamDTO>> getTeamsByProjectId(@PathVariable Long projectId) {
        try {
            List<TeamDTO> teams = projectService.getTeamsByProjectId(projectId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}




