package org.diploma.fordiplom.service.impl;

import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.DTO.FileDTO;
import org.diploma.fordiplom.entity.FileEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.TaskFileRelation;
import org.diploma.fordiplom.repository.FileRepository;
import org.diploma.fordiplom.repository.TaskFileRelationRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.diploma.fordiplom.entity.FileEntity.calculateFileHash;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private  FileRepository fileRepository;
    @Autowired
    private  TaskRepository taskRepository;
    @Autowired
    private  TaskFileRelationRepository relationRepository;

    @Override
    @Transactional
    public FileEntity uploadFile(MultipartFile file) {
        try {
            byte[] fileData = file.getBytes();
            String fileHash = calculateFileHash(fileData);

            FileEntity existingFile = fileRepository.findByFileHash(fileHash);
            if (existingFile != null) {

                return existingFile;
            }

            // Если файл новый, сохраняем его
            FileEntity entity = new FileEntity();
            entity.setFileName(file.getOriginalFilename());
            entity.setContentType(file.getContentType());
            entity.setData(fileData);
            entity.setUploadedAt(Instant.now());
            entity.setFileHash(fileHash); // Сохраняем хеш файла
            return fileRepository.save(entity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    @Transactional
    public void attachFileToTask(Long taskId, Long fileId) {
        TaskEntity task = taskRepository.findById(taskId).orElseThrow();
        FileEntity file = fileRepository.findById(fileId).orElseThrow();

        TaskFileRelation relation = new TaskFileRelation();
        relation.setTask(task);
        relation.setFile(file);

        relationRepository.save(relation);
    }

    @Override
    public List<FileDTO> getFilesByTaskId(Long taskId) {
        return relationRepository.findByTaskId(taskId).stream()
                .map(relation -> {
                    FileEntity file = relation.getFile();
                    return new FileDTO(file.getId(), file.getFileName(), file.getContentType(), file.getUploadedAt());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFileFromTask(Long taskId, Long fileId) {
        relationRepository.deleteByTaskIdAndFileId(taskId, fileId);
    }

    @Override
    public FileEntity getFileById(Long fileId) {
        return fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    }
}