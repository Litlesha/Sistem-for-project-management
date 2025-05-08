package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.CommentEntity;

import java.util.List;

public interface CommentService {
    List<CommentEntity> getCommentsByTaskId(Long taskId);
    CommentEntity addComment(Long taskId, String text, String email);
}