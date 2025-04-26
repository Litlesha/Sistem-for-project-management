package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.repository.SprintRepository;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SprintServiceImpl implements SprintService {

    @Autowired private SprintRepository sprintRepository;
    @Autowired
    private ProjectService projectService;

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
        return sprintRepository.save(sprintEntity);
    }
    @Override
    public SprintEntity updateSprint(SprintEntity sprint){return null;}
    @Override
    public SprintEntity getSprintById(Long id){return sprintRepository.findById(id).orElse(null);}
    @Override
    public List<SprintEntity> getSprintByProjectId(Long projectId){return sprintRepository.findByProjectId(projectId);}

    @Override
    public List<SprintResponse> getSprintsByProjectId(Long projectId) {
        List<SprintEntity> entities = sprintRepository.findByProjectId(projectId);
        return entities.stream().map(this::mapToDto).toList();
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
