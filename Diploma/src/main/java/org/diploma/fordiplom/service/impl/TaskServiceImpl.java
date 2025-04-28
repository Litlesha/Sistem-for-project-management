package org.diploma.fordiplom.service.impl;


import org.diploma.fordiplom.entity.DTO.request.TaskRequest;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.repository.SprintRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintRepository sprintRepository;

    @Override
    public TaskEntity createTask(TaskRequest request){
        TaskEntity task = new TaskEntity();
        task.setTitle(request.getTitle());
        task.setTaskType(request.getTask_type());
        task.setTaskKey(keyGenerator(request));
        if (request.getSprintId() != null) {
            SprintEntity sprint = sprintService.getSprintById(request.getSprintId());
            task.setSprint(sprint);
        } else {
            task.setSprint(null); // Явно укажем null, чтобы было понятно
        }

        task.setProject(projectService.getProjectById(request.getProjectId()));
        return taskRepository.save(task);
    }


    @Override
    public String keyGenerator(TaskRequest request) {
        String projectKey = projectService.getProjectKey(request.getProjectId());

        if (projectKey == null || projectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ проекта не найден для ID: " + request.getProjectId());
        }

        List<String> taskKeys = taskRepository.findTaskKeysByProjectKey(projectKey);
        int nextNumber = 1;

        if (!taskKeys.isEmpty()) {
            String lastKey = taskKeys.get(0); // Самый последний по номеру
            String[] parts = lastKey.split("-");
            if (parts.length == 2) {
                try {
                    nextNumber = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    // Если вдруг формат битый
                    nextNumber = 1;
                }
            }
        }

        return projectKey + "-" + nextNumber;
    }

    @Override
    public List<TaskEntity> getTasksBySprintId(Long sprintId) {
        return taskRepository.findBySprintId(sprintId);
    }

    @Override
    public List<TaskEntity> getBackLogTasksByProjectId(Long projectId){
        return taskRepository.findByProject_IdAndSprintIsNull(projectId);
    }

    @Override
    public void updateTaskLocation(Long taskId, Long sprintId) {
        TaskEntity task = taskRepository.findById(taskId).get();

        // Если sprintId не null, обновляем привязку задачи к спринту
        if (sprintId != null) {
            SprintEntity sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(() -> new RuntimeException("Спринт не найден"));
            task.setSprint(sprint);
        } else {
            task.setSprint(null); // Если задача возвращается в бэклог, убираем привязку
        }

        taskRepository.save(task); // Сохраняем обновлённую задачу
    }
    }

