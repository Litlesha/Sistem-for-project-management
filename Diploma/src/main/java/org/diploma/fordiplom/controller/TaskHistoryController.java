package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.DTO.TaskHistoryEntryDTO;
import org.diploma.fordiplom.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskHistoryController {
    @Autowired
    private TaskHistoryService historyService;

    @GetMapping("/{taskId}/history")
    public List<TaskHistoryEntryDTO> getTaskHistory(@PathVariable Long taskId) {
        return historyService.getHistoryForTask(taskId);
    }
}