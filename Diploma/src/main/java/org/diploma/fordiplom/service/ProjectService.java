package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.ProjectDTO;
import org.diploma.fordiplom.entity.DTO.ProjectSummaryDTO;
import org.diploma.fordiplom.entity.DTO.TeamDTO;
import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.TeamEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProjectService {
    public ProjectEntity createProject(ProjectRequest newProject, String creatorEmail);
    public ProjectEntity getProjectById(Long id);
    public List<ProjectEntity> getProjectsByUserEmail(String email);
    String getProjectKey(Long id);
    void addTeamToProject(Long projectId, Long teamId);
    List<TeamDTO> getTeamsByProjectId(Long projectId);
    ProjectDTO getProjectByTaskId(Long taskId);
    ProjectSummaryDTO getProjectSummary(Long projectId);
    void completeProject(Long projectId);
}
