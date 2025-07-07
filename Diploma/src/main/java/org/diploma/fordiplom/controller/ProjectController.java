package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.DTO.*;
import org.diploma.fordiplom.entity.DTO.request.AddTeamToProjectRequest;
import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.ProjectUserRoleEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.PURService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Autowired
    private PURService purService;

    @PostMapping(path = "/create_project", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectEntity createNewProject(@RequestBody ProjectRequest projectRequest, Principal principal) {
        return projectService.createProject(projectRequest, principal.getName());
    }
    @GetMapping("/api/project/{id}/backlog")
    public ResponseEntity<ProjectEntity> getProjectById(
            @PathVariable Long id,
            Principal principal) {

        ProjectEntity project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        String email = principal.getName();
        boolean accessAllowed = project.getUsers().stream()
                .anyMatch(user -> user.getEmail().equals(email));

        if (!accessAllowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(project);
    }
    @GetMapping("/api/project/{id}/board")
    public ResponseEntity<ProjectEntity> getProjectBoardById(@PathVariable Long id, Principal principal) {
        ProjectEntity project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        String email = principal.getName();
        boolean accessAllowed = project.getUsers().stream()
                .anyMatch(user -> user.getEmail().equals(email));

        if (!accessAllowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(project);
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
    public ResponseEntity<String> addTeamToProject(@RequestBody AddTeamToProjectRequest request, Principal principal) {
        Long projectId = request.getProjectId();
        Long teamId = request.getTeamId();
        String userEmail = principal.getName();


        boolean assignedUser = purService.checkAccess(projectId, userService.getUserByEmail(userEmail).getId_user());

        if (!assignedUser) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас нет прав для добавления команды в проект.");
        }

        try {
            projectService.addTeamToProject(projectId, teamId);
            return ResponseEntity.ok("Команда успешно добавлена в проект");
        } catch (RuntimeException e) {
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
    @GetMapping("/api/project/{projectId}/summary")
    public ProjectSummaryDTO getProjectSummary(@PathVariable Long projectId) {
        return projectService.getProjectSummary(projectId);
    }
    @PostMapping("/api/project/{projectId}/complete")
    public ResponseEntity<?> completeProject(@PathVariable Long projectId, Principal principal) {
        String email = principal.getName();
        boolean assignedUser = purService.checkAccess(projectId, userService.getUserByEmail(email).getId_user());
        if (!assignedUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        projectService.completeProject(projectId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/api/project/recent")
    public List<ProjectWithTaskCountDTO> getRecentProjects(Principal principal) {
        String username = principal.getName();
        return projectService.getRecentProjectsForUser(username);
    }
}




