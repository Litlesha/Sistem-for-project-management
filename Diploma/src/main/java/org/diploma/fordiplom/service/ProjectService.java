package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProjectService {
    public ProjectEntity createProject(ProjectRequest newProject, String creatorEmail);
    public ProjectEntity getProjectById(Long id);
    public List<ProjectEntity> getProjectsByUserEmail(String email);
    String getProjectKey(Long id);
}
