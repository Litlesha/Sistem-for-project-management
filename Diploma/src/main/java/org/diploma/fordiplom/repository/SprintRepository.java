package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SprintRepository extends JpaRepository<SprintEntity, Long> {
    @Query ("SELECT s FROM SprintEntity s JOIN s.project p WHERE p.id = :id_project")
    List<SprintEntity> findByProjectId(@Param("id_project") Long id_project);

    <T> SprintEntity findByProjectIdAndIsActiveTrue(long projectId);
}
