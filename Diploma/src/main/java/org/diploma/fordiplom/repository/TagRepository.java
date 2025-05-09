package org.diploma.fordiplom.repository;

import org.aspectj.apache.bcel.generic.Tag;
import org.diploma.fordiplom.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
    List<TagEntity> findByNameContainingIgnoreCase(String query);
    Optional<TagEntity> findByName(String name);
}