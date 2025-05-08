package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.CommentEntity;
import org.diploma.fordiplom.entity.DTO.CommentDTO;
import org.diploma.fordiplom.entity.DTO.request.CommentRequest;
import org.diploma.fordiplom.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
@RestController
public class CommentController {
    @Autowired
    private  CommentService commentService;

    @GetMapping("/api/tasks/{taskId}/comments/active")
    public List<CommentEntity> getComments(@PathVariable Long taskId) {
        return commentService.getCommentsByTaskId(taskId);
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentRequest request,
            Principal principal
    ) {
        if (taskId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String email = principal.getName();
        CommentEntity saved = commentService.addComment(taskId, request.getText(), email);
        return ResponseEntity.ok(new CommentDTO(saved));
    }

}
