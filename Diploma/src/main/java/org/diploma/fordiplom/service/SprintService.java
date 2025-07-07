package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.SprintDTO;
import org.diploma.fordiplom.entity.DTO.SprintSummaryDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.SprintRequest;
import org.diploma.fordiplom.entity.DTO.response.SprintResponse;
import org.diploma.fordiplom.entity.ProjectEntity;
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
    List<TaskDTO> completeSprint(Long sprintId, List<Long> tasksToBacklog);
    boolean isUserInSprint(String email, Long sprintId);
    SprintEntity updateSprint(Long sprintId, SprintRequest request);
    List<SprintDTO> getActiveSprintsWithTasks(Long projectId);
    SprintSummaryDTO getSprintSummary(Long sprintId);
    List<SprintDTO> getActiveSprintsForUser(Long projectId, String email);
    Long getProjectIdBySprintId(Long sprintId);
    List<SprintEntity> getAllByProjectId(Long projectId);
    SprintDTO getSprintWithTasks(Long sprintId);
    List<SprintDTO> getActiveSprintsByProject(Long projectId);
}
