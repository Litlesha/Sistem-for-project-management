package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.SprintEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    @Query("SELECT t.taskKey FROM TaskEntity t WHERE t.taskKey LIKE CONCAT(:projectKey, '-%') ORDER BY LENGTH(t.taskKey) DESC, t.taskKey DESC")
    List<String> findTaskKeysByProjectKey(@Param("projectKey") String projectKey);
    @Query("SELECT t FROM TaskEntity t " +
            "WHERE t.sprint.id = :sprintId " +
            "AND (t.isCompleted = false OR t.isCompleted IS NULL) " +
            "ORDER BY t.status ASC, t.position ASC")
    List<TaskEntity> findBySprintIdAndIsCompletedFalse(Long sprintId);
    List<TaskEntity> findByProject_IdAndSprintIsNullOrderByPositionAsc(Long projectId);
    @Query("SELECT COALESCE(MAX(t.position), 0) FROM TaskEntity t WHERE t.sprint IS NULL")
    int findMaxPositionInSprint(@Param("sprintId") Long sprintId);
    @Query("""
    SELECT t FROM TaskEntity t
    WHERE LOWER(t.title) LIKE LOWER(CONCAT(:query, '%'))
      AND t.project.id = :projectId
      AND t.sprint.id = :sprintId
""")
    List<TaskEntity> searchInSprint(@Param("query") String query,
                                    @Param("projectId") Long projectId,
                                    @Param("sprintId") Long sprintId);
    List<TaskEntity> findBySprintOrderByPositionAsc(SprintEntity sprint);
    int countByProjectIdAndIsCompletedTrue(Long projectId);
}
