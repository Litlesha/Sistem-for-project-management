package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/api/project/{id}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long id) {
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
}




