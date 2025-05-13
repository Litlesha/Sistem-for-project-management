package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.ActionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionTypeRepository extends JpaRepository<ActionTypeEntity, Long> {
    Optional<ActionTypeEntity> findByCode(String code);
}
