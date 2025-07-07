package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.RoleEntity;
import org.diploma.fordiplom.repository.RoleRepository;
import org.diploma.fordiplom.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleEntity getRoleByName(String roleName) {
        return roleRepository.findByName(roleName).orElse(null);
    }
}
