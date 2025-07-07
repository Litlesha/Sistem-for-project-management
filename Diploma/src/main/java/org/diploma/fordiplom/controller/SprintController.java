package org.diploma.fordiplom.controller;


import org.diploma.fordiplom.entity.DTO.SprintDTO;
import org.diploma.fordiplom.entity.DTO.SprintSummaryDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.CompleteSprintRequest;
import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.service.PURService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class SprintController {
    @Autowired
    SprintService sprintService;
    @Autowired
    private PURService purService;
    @Autowired
    private UserService userService;

    @PostMapping(path = "/create_sprint", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SprintEntity createNewSprint(@RequestBody SprintRequest sprint, Principal principal) {
        Long projectId = sprint.getProjectId();
        String user = principal.getName();

        boolean assignedUser = purService.checkAccess(projectId, userService.getUserByEmail(user).getId_user());
        if (!assignedUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return sprintService.createSprint(sprint);
    }
    @GetMapping("/api/project/{projectId}/sprints")
    public List<SprintResponse> getSprintsByProjectId(@PathVariable Long projectId) {
        return sprintService.getSprintsByProjectId(projectId);
    }

    @PostMapping("/api/sprint/{sprintId}/start")
    public void startSprint(@PathVariable Long sprintId, Principal principal) {
        Long projectId = sprintService.getProjectIdBySprintId(sprintId);
        String user = principal.getName();

        boolean assignedUser = purService.checkAccess(projectId, userService.getUserByEmail(user).getId_user());
        if (!assignedUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        sprintService.startSprint(sprintId);
    }
    @GetMapping("/api/sprint/{sprintId}")
    public ResponseEntity<SprintDTO> getSprintById(@PathVariable Long sprintId) {
        SprintEntity sprint = sprintService.getSprintById(sprintId);
        if (sprint == null) {
            return ResponseEntity.notFound().build();
        }
        SprintDTO sprintDTO = new SprintDTO(sprint);
        return ResponseEntity.ok(sprintDTO);
    }
    @PostMapping("/api/sprint/{sprintId}/complete")
    public ResponseEntity<List<TaskDTO>> completeSprint(
            @PathVariable Long sprintId,
            @RequestBody CompleteSprintRequest request,
            Principal principal) {

        String user = principal.getName();
        Long projectId = request.getProjectId();
        List<Long> taskIds = request.getTaskIds();

        boolean assignedUser = purService.checkAccess(projectId, userService.getUserByEmail(user).getId_user());
        if (!assignedUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<TaskDTO> updatedTasks = sprintService.completeSprint(sprintId, taskIds);
        return ResponseEntity.ok(updatedTasks);
    }

    @PutMapping("/api/sprint/{sprintId}/update")
    public SprintEntity updateSprint(@PathVariable Long sprintId, @RequestBody SprintRequest request) {
        return sprintService.updateSprint(sprintId, request);
        }
    @GetMapping("/api/sprint/active/{projectId}")
    public ResponseEntity<List<SprintDTO>> getActiveSprintWithTasks(@PathVariable Long projectId) {
        try {
            List<SprintDTO> sprintDTO = sprintService.getActiveSprintsWithTasks(projectId);
            return ResponseEntity.ok(sprintDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/api/projects/{projectId}/sprints/active")
    public ResponseEntity<List<SprintDTO>> getActiveSprints(@PathVariable Long projectId) {
        List<SprintDTO> activeSprints = sprintService.getActiveSprintsByProject(projectId);
        return ResponseEntity.ok(activeSprints);
    }

    @GetMapping("/api/sprint/{sprintId}/summary")
    public SprintSummaryDTO getSprintSummary(@PathVariable Long sprintId) {
        return sprintService.getSprintSummary(sprintId);
    }

}


