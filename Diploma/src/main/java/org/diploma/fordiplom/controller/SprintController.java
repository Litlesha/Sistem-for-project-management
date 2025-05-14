package org.diploma.fordiplom.controller;


import org.diploma.fordiplom.entity.DTO.SprintDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
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
public class SprintController {
    @Autowired
    SprintService sprintService;

    @PostMapping(path = "/create_sprint", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SprintEntity createNewSprint(@RequestBody SprintRequest sprint) {
        return sprintService.createSprint(sprint);
    }
    @GetMapping("/api/project/{projectId}/sprints")
    public List<SprintResponse> getSprintsByProjectId(@PathVariable Long projectId) {
        return sprintService.getSprintsByProjectId(projectId);
    }
    @PostMapping("/api/sprint/{sprintId}/start")
    public void startSprint(@PathVariable Long sprintId) {
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
    public ResponseEntity<List<TaskDTO>> completeSprint(@PathVariable Long sprintId) {
        List<TaskDTO> updatedTasks = sprintService.completeSprint(sprintId);
        return ResponseEntity.ok(updatedTasks);
    }
    @PutMapping("/api/sprint/{sprintId}/update")
    public SprintEntity updateSprint(@PathVariable Long sprintId, @RequestBody SprintRequest request) {
        return sprintService.updateSprint(sprintId, request);
        }
    @GetMapping("/api/sprint/active/{projectId}")
    public ResponseEntity<SprintDTO> getActiveSprintWithTasks(@PathVariable Long projectId) {
        try {
            SprintDTO sprintDTO = sprintService.getActiveSprintWithTasks(projectId);
            return ResponseEntity.ok(sprintDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    }


