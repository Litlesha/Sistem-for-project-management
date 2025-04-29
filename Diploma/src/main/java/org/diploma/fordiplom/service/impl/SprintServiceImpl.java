package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.DTO.SprintDTO;
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

import java.util.List;

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
    public SprintDTO getActiveSprintWithTasks(Long projectId) {
        SprintEntity activeSprint = sprintRepository.findActiveSprintByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Активный спринт не найден"));

        List<TaskEntity> tasks = taskRepository.findBySprintId(activeSprint.getId());

        // Создаём DTO для активного спринта
        SprintDTO sprintDTO = new SprintDTO(activeSprint.getId(), activeSprint.getSprintName(), activeSprint.getStartDate(),
                activeSprint.getEndDate(), activeSprint.getGoal(), activeSprint.getDuration(), activeSprint.getIsActive());

        // Преобразуем задачи в список DTO (если нужно)
        sprintDTO.setTasks(tasks);  // Нужно создать соответствующий TaskDTO, если нужно преобразовать TaskEntity в DTO

        return sprintDTO;
    }

    @Override
    public SprintEntity getSprintById(Long id){return sprintRepository.findById(id).orElse(null);}
    @Override
    public List<SprintEntity> getSprintByProjectId(Long projectId){return sprintRepository.findByProjectId(projectId);}

    @Override
    public List<SprintResponse> getSprintsByProjectId(Long projectId) {
        List<SprintEntity> entities = sprintRepository.findByProjectId(projectId);
        return entities.stream().map(this::mapToDto).toList();
    }

    public void startSprint(Long sprintId) {
        SprintEntity sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        sprint.setIsActive(true);
        sprintRepository.save(sprint);

        // Обновляем все задачи, связанные с этим спринтом
        List<TaskEntity> tasks = taskRepository.findBySprintId(sprintId); // Получаем все задачи для текущего спринта
        for (TaskEntity task : tasks) {
            task.setSprint(sprint); // Привязываем задачи к текущему спринту
        }
        taskRepository.saveAll(tasks); // Сохраняем все обновленные задачи
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
