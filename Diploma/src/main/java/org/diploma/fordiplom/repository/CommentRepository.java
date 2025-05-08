package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}