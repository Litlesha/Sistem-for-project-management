package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.TaskHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TaskHistoryEntryRepository extends JpaRepository<TaskHistoryEntry, Long> {
    List<TaskHistoryEntry> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
