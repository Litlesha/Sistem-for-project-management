package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.ProjectRepository;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserService userService;

    private ProjectEntity projectEntity;

    public ProjectEntity createProject(ProjectRequest request, String creatorEmail) {
        UserEntity creator = userService.getUserByEmail(creatorEmail);
        if (creator == null) {
            throw new IllegalArgumentException("Создатель проекта не найден");
        }
        ProjectEntity project = new ProjectEntity();
        project.setName(request.getName());
        project.setKey(request.getKey());
        project.setDescription(request.getDescription());

        Set<UserEntity> users = new HashSet<>();
        users.add(creator); // добавляем создателя
        project.setUsers(users);
        return projectRepository.save(project);
    }

    @Override
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public String getProjectKey(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return project.getKey();
    }


    @Override
    public List<ProjectEntity> getProjectsByUserEmail(String email) {
        return projectRepository.findByUserEmail(email);
    }

}
