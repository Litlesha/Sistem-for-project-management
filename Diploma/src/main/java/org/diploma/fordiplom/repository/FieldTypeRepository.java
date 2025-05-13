package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.FieldTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FieldTypeRepository extends JpaRepository<FieldTypeEntity, Long> {
    Optional<FieldTypeEntity> findByCode(String code);
}
