package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.DTO.SprintDTO;
import org.diploma.fordiplom.entity.DTO.SprintSummaryDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.repository.SprintRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintServiceImpl implements SprintService {

    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    TaskRepository taskRepository;


    @Override
    public SprintEntity createSprint(SprintRequest request){
        SprintEntity sprintEntity = new SprintEntity();
        sprintEntity.setSprintName(request.getSprintName());
        sprintEntity.setGoal(request.getGoal());
        sprintEntity.setDuration(request.getDuration());
        sprintEntity.setStartDate(request.getStartDate());
        sprintEntity.setEndDate(request.getEndDate());
        ProjectEntity project = projectService.getProjectById(request.getProjectId());
        sprintEntity.setProject(project);
        sprintEntity.setIsActive(false);
        return sprintRepository.save(sprintEntity);
    }
    @Override
    public SprintEntity updateSprint(Long sprintId, SprintRequest request){
        SprintEntity updSprint = sprintRepository.findById(sprintId).get();
        updSprint.setSprintName(request.getSprintName());
        updSprint.setGoal(request.getGoal());
        updSprint.setDuration(request.getDuration());
        updSprint.setStartDate(request.getStartDate());
        updSprint.setEndDate(request.getEndDate());
        return sprintRepository.save(updSprint);}

    @Override
    public List<SprintDTO> getActiveSprintsWithTasks(Long projectId) {
        List<SprintEntity> activeSprints = sprintRepository.findByProjectIdAndIsActiveTrueAndIsCompletedFalse(projectId);

        if (activeSprints.isEmpty()) {
            throw new RuntimeException("Нет активных спринтов");
        }

        return activeSprints.stream().map(sprint -> {
            List<TaskEntity> tasks = taskRepository.findBySprintIdAndIsCompletedFalse(sprint.getId());

            SprintDTO sprintDTO = new SprintDTO(
                    sprint.getId(),
                    sprint.getSprintName(),
                    sprint.getStartDate(),
                    sprint.getEndDate(),
                    sprint.getGoal(),
                    sprint.getDuration(),
                    sprint.getIsActive()
            );

            List<TaskDTO> taskDTOs = tasks.stream()
                    .map(TaskDTO::new)
                    .collect(Collectors.toList());

            sprintDTO.setTasks(taskDTOs);
            return sprintDTO;
        }).collect(Collectors.toList());
    }

    public SprintSummaryDTO getSprintSummary(Long sprintId) {
        List<TaskEntity> allTasks = taskRepository.findBySprintIdAndIsCompletedFalse(sprintId);

        int doneCount = 0;
        int openCount = 0;
        List<TaskDTO> openTasks = new ArrayList<>();

        for (TaskEntity task : allTasks) {
            String status = task.getStatus();
            Boolean isCompleted = task.getIsCompleted();

            // Считаем задачи со статусом "Выполнено" как выполненные
            if ("Выполнено".equalsIgnoreCase(status) || Boolean.TRUE.equals(isCompleted)) {
                doneCount++;
            } else if ("К выполнению".equalsIgnoreCase(status) || "В работе".equalsIgnoreCase(status)) {
                // В списке для выбора только задачи со статусом "К выполнению" и "В работе"
                openCount++;
                openTasks.add(new TaskDTO(task));
            }
            // Если статус другой — просто игнорируем (не считаем в doneCount и openCount)
        }

        return new SprintSummaryDTO(doneCount, openCount, openTasks);
    }


    @Override
    public SprintEntity getSprintById(Long id){return sprintRepository.findById(id).orElse(null);}
    @Override
    public List<SprintEntity> getSprintByProjectId(Long projectId){return sprintRepository.findByProjectId(projectId);}

    @Override
    public List<SprintResponse> getSprintsByProjectId(Long projectId) {
        List<SprintEntity> entities = sprintRepository.findByProjectIdAndIsCompletedFalseAndIsActiveFalse(projectId);
        return entities.stream().map(this::mapToDto).toList();
    }

    public void startSprint(Long sprintId) {
        SprintEntity sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        sprint.setIsActive(true);
        sprintRepository.save(sprint);

        List<TaskEntity> tasks = taskRepository.findBySprintIdAndIsCompletedFalse(sprintId);
        int position = 0;
        for (TaskEntity task : tasks) {
            task.setSprint(sprint);
            task.setStatus("К выполнению");
            task.setPosition(position++); // Устанавливаем позицию внутри спринта
        }
        taskRepository.saveAll(tasks);
    }

    public List<TaskDTO> completeSprint(Long sprintId, List<Long> tasksToBacklog) {
        SprintEntity sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        sprint.setIsActive(false);
        sprint.setIsCompleted(true);
        sprintRepository.save(sprint);

        List<TaskEntity> tasks = taskRepository.findBySprintIdAndIsCompletedFalse(sprintId);
        int maxBacklogPosition = taskRepository.findMaxPositionInSprint(null);
        List<TaskDTO> updatedTasks = new ArrayList<>();

        for (TaskEntity task : tasks) {
            if (tasksToBacklog.contains(task.getId())) {
                task.setSprint(null); // Вернуть в бэклог
                task.setPosition(++maxBacklogPosition);
            } else {
                task.setIsCompleted(true); // Удалить/завершить
            }
            taskRepository.save(task);
            updatedTasks.add(new TaskDTO(task));
        }

        return updatedTasks;
    }

    private SprintResponse mapToDto(SprintEntity entity) {
        SprintResponse dto = new SprintResponse();
        dto.setId(entity.getId());
        dto.setSprintName(entity.getSprintName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setGoal(entity.getGoal());
        dto.setDuration(entity.getDuration());
        return dto;
    }
}
