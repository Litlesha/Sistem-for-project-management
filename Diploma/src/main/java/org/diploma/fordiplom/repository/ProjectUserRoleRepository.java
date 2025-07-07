package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.ProjectUserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRoleEntity, Long> {
    @Query("SELECT p FROM ProjectUserRoleEntity p WHERE p.project.id = :projectId AND p.user.id_user = :userId")
    Optional<ProjectUserRoleEntity> findByProjectIdAndUserIdUser(@Param("projectId") Long projectId, @Param("userId") Long userId);

}
