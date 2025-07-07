package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<SprintEntity, Long> {
    @Query("SELECT s.id FROM SprintEntity s WHERE s.project.id = :projectId AND s.isActive = true and s.isCompleted = false")
    List<Long> findActiveSprintIdsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT s FROM SprintEntity s JOIN s.project p WHERE p.id = :id_project AND (s.isActive IS NULL OR s.isActive = false) and s.isCompleted = false ")
    List<SprintEntity> findByProjectId(@Param("id_project") Long id_project);

    @Query("SELECT s FROM SprintEntity s WHERE s.project.id = :projectId AND s.isActive = true AND s.isCompleted = false")
    List<SprintEntity> findByProjectIdAndIsActiveTrueAndIsCompletedFalse(@Param("projectId") Long projectId);

    @Query("""
    SELECT s FROM SprintEntity s 
    JOIN s.project p 
    WHERE p.id = :id_project 
    AND (s.isActive IS NULL OR s.isActive = false) 
    AND s.isCompleted = false
""")
    List<SprintEntity> findIncompleteInactiveSprintsByProjectId(@Param("id_project") Long id_project);

    @Query("SELECT s FROM SprintEntity s WHERE s.project.id = :projectId")
    List<SprintEntity> findAllByProjectId(@Param("projectId") Long projectId);

    List<SprintEntity> findByProjectIdAndIsCompletedFalseAndIsActiveFalse(Long projectId);
    int countByProjectIdAndIsCompletedTrue(Long projectId);
    List<SprintEntity> findByProjectIdAndIsActiveTrue(Long projectId);
    @Query("""
    select count(s) from SprintEntity s
    where s.project.id = :projectId
      and s.isActive = true
      and (s.isCompleted = false or s.isCompleted is null)
""")
    long countActiveSprintsByProjectId(@Param("projectId") Long projectId);
}
