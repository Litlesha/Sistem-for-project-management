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
import org.diploma.fordiplom.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
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
    @Autowired
    TaskHistoryService taskHistoryService;

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

            // Путь для хранения файла
            String uploadDir = "uploaded-files/";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String fullPath = uploadDir + fileName;

            // Убедитесь, что папка существует
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Сохраняем файл на диск
            File destination = new File(fullPath);
            try (FileOutputStream fos = new FileOutputStream(destination)) {
                fos.write(fileData);
            }

            // Сохраняем информацию в БД
            FileEntity entity = new FileEntity();
            entity.setFileName(file.getOriginalFilename());
            entity.setContentType(file.getContentType());
            entity.setUploadedAt(Instant.now());
            entity.setFileHash(fileHash);
            entity.setFilePath(fullPath);

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

        // Старое состояние файлов задачи (пустое)
        String oldFileNames = "";  // Старое состояние файлов - пустая строка

        // Создание связи между файлом и задачей
        TaskFileRelation relation = new TaskFileRelation();
        relation.setTask(task);
        relation.setFile(file);
        task.setUpdatedAt(Instant.now());
        relationRepository.save(relation);

        // Новое состояние файлов задачи (только имя добавленного файла)
        String newFileNames = file.getFileName();  // Имя добавленного файла

        // Получаем email текущего пользователя
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Добавление записи в историю
        taskHistoryService.saveTaskHistory(
                task,
                oldFileNames,                 // Старое состояние файлов (пустое)
                newFileNames,                 // Новое состояние файлов
                email,
                "create",                     // Тип действия
                "file"                        // Поле, которое изменилось
        );
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
        TaskEntity task = taskRepository.findById(taskId).orElseThrow();
        FileEntity file = fileRepository.findById(fileId).orElseThrow();

        // Старое состояние файлов задачи (до удаления)
        String oldFileNames = file.getFileName();

        // Удаление связи между файлом и задачей
        relationRepository.deleteByTaskIdAndFileId(taskId, fileId);
        task.setUpdatedAt(Instant.now());

        // Новое состояние файлов задачи (после удаления)
        String newFileNames = "";

        // Получаем email текущего пользователя
        String email = SecurityContextHolder.getContext().getAuthentication().getName();


        // Добавление записи в историю
        taskHistoryService.saveTaskHistory(
                task,
                oldFileNames,                 // Старое состояние файлов
                newFileNames,                 // Новое состояние файлов
                email,
                "delete",                     // Тип действия
                "file"                        // Поле, которое изменилось
        );
    }

    @Override
    public FileEntity getFileById(Long fileId) {
        return fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    }
}