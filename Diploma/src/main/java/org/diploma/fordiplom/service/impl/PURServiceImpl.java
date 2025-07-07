package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.ProjectUserRoleEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.ProjectUserRoleRepository;
import org.diploma.fordiplom.service.PURService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.RoleService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


@Service
public class PURServiceImpl implements PURService {

    @Autowired
    ProjectUserRoleRepository projectUserRoleRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RoleService roleService;

    @Override
    public ProjectUserRoleEntity getProjectUserRole(Long projectId, Long userId) {
        return projectUserRoleRepository.findByProjectIdAndUserIdUser(projectId, userId)
                .isPresent() ? projectUserRoleRepository.findByProjectIdAndUserIdUser(projectId, userId).get() : null;
    }

    @Override
    public ProjectUserRoleEntity setRoleForUser(Long projectId, String email, String role) {
        ProjectUserRoleEntity projectUserRoleEntity = new ProjectUserRoleEntity();
        projectUserRoleEntity.setProject(projectService.getProjectById(projectId));
        projectUserRoleEntity.setUser(userService.getUserByEmail(email));
        projectUserRoleEntity.setRole(roleService.getRoleByName(role));

        return projectUserRoleRepository.save(projectUserRoleEntity);
    }

    @Override
    public boolean checkAccess(Long projectId, Long userId) {
        ProjectUserRoleEntity role = getProjectUserRole(projectId, userId);
        return role.getRole().getName().equals("PROJECT_ADMIN");
    }
}
