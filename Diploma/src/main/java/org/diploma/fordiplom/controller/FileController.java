package org.diploma.fordiplom.controller;

import lombok.RequiredArgsConstructor;
import org.diploma.fordiplom.entity.DTO.FileDTO;
import org.diploma.fordiplom.entity.FileEntity;
import org.diploma.fordiplom.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    @PostMapping("/{taskId}/attach/{fileId}")
    public ResponseEntity<Void> attachFileToTask(@PathVariable Long taskId, @PathVariable Long fileId) {
        fileService.attachFileToTask(taskId, fileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<FileDTO>> getFilesByTask(@PathVariable Long taskId) {
        List<FileDTO> fileDTOs = fileService.getFilesByTaskId(taskId);
        return ResponseEntity.ok(fileDTOs);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        FileEntity file = fileService.getFileById(fileId);

        String encodedFilename = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getData());
    }

    @DeleteMapping("/{taskId}/detach/{fileId}")
    public ResponseEntity<Void> deleteFileFromTask(@PathVariable Long taskId, @PathVariable Long fileId) {
        fileService.deleteFileFromTask(taskId, fileId);
        return ResponseEntity.noContent().build();
    }
}
