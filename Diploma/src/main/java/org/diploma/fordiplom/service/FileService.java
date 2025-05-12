package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.FileDTO;
import org.diploma.fordiplom.entity.FileEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface FileService {
    FileEntity uploadFile(MultipartFile file);
    void attachFileToTask(Long taskId, Long fileId);
    List<FileDTO> getFilesByTaskId(Long taskId);
    void deleteFileFromTask(Long taskId, Long fileId);
    FileEntity getFileById(Long fileId);
}
