package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
import org.diploma.fordiplom.entity.SprintEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface SprintService {
    SprintEntity createSprint(SprintRequest request);
    SprintEntity getSprintById(Long id);
    public List<SprintEntity> getSprintByProjectId(Long projectId);
    List<SprintResponse> getSprintsByProjectId(Long projectId);
    void startSprint(Long sprintId);
    SprintEntity updateSprint(Long sprintId, SprintRequest request);
}
