package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.RoleEntity;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    RoleEntity getRoleByName(String roleName);
}
