package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findByFileHash(String fileHash);
}