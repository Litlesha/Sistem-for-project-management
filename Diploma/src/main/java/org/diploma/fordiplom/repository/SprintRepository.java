package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<SprintEntity, Long> {
    @Query("SELECT s FROM SprintEntity s JOIN s.project p WHERE p.id = :id_project AND (s.isActive IS NULL OR s.isActive = false) and s.isCompleted = false ")
    List<SprintEntity> findByProjectId(@Param("id_project") Long id_project);
    @Query("SELECT s FROM SprintEntity s WHERE s.project.id = :projectId AND s.isActive = true AND s.isCompleted = false")
    Optional<SprintEntity> findActiveSprintByProjectId(@Param("projectId") Long projectId);
    List<SprintEntity> findByProjectIdAndIsCompletedFalseAndIsActiveFalse(Long projectId);
}
