package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.TaskFileRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
    public interface TaskFileRelationRepository extends JpaRepository<TaskFileRelation, Long> {
        List<TaskFileRelation> findByTaskId(Long taskId);
        List<TaskFileRelation> findByFileId(Long fileId);
        void deleteByTaskIdAndFileId(Long taskId, Long fileId);
    }

