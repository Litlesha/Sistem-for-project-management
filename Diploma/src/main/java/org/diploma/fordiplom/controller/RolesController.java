package org.diploma.fordiplom.controller;


import org.diploma.fordiplom.entity.*;
import org.diploma.fordiplom.entity.DTO.request.RoleRequest;
import org.diploma.fordiplom.service.PURService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.TeamService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RequestMapping("/api/roles")
@RestController
public class RolesController {
    @Autowired
    private PURService purService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @PostMapping(path = "/set_creator_role", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ProjectUserRoleEntity setCreatorRole(@RequestParam Long projectId, Principal principal) {
        return purService.setRoleForUser(projectId, principal.getName(), "PROJECT_ADMIN");
    }

    @PostMapping(path = "/set_users_role")
    public ResponseEntity<?> setUsersRole(@RequestBody RoleRequest request) {
        Long projectId = request.getProjectId();
        Long teamId = request.getTeamId();

        List<UserEntity> users = teamService.getUsersForTeam(teamId);

        String projectCreatorEmail = projectService.getCreator(projectId).getEmail();

        List<ProjectUserRoleEntity> assignedRoles = new ArrayList<>();

        for (UserEntity user : users) {
            if (user.getEmail().equals(projectCreatorEmail)) {
                continue;
            }

            ProjectUserRoleEntity role = purService.setRoleForUser(projectId, user.getEmail(), "PROJECT_DEVELOPER");
            assignedRoles.add(role);
        }

        return ResponseEntity.ok(assignedRoles);
    }

    @PostMapping(path = "/set_role_by_admin")
    public ProjectUserRoleEntity setRoleByAdmin(@RequestParam Long projectId) {
        return null;
    }

    @GetMapping(path = "/getUserRole/{projectId}")
    public String getUserRole(@PathVariable Long projectId, Principal principal) {
        String email = principal.getName();
        String role = purService.getProjectUserRole(projectId, userService.getUserByEmail(email).getId_user()).getRole().getName();
        return role;
    }
}
