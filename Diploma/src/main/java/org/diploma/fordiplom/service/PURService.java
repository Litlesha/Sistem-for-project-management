package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.ProjectUserRoleEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;


public interface PURService {
    ProjectUserRoleEntity getProjectUserRole(Long projectId, Long userId);
    ProjectUserRoleEntity setRoleForUser(Long projectId, String email, String role);
    boolean checkAccess(Long projectId, Long userId);
}
