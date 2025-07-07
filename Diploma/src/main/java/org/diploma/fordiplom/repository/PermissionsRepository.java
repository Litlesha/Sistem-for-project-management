package org.diploma.fordiplom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.diploma.fordiplom.entity.PermissionEntity;

import java.util.Optional;

public interface PermissionsRepository extends JpaRepository<PermissionEntity, Integer> {
    Optional<PermissionEntity> findByCode(String code);
}
