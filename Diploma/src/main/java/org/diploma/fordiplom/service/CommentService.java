package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.CommentEntity;
import org.diploma.fordiplom.entity.DTO.CommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByTaskId(Long taskId);
    CommentEntity addComment(Long taskId, String text, String email);
}