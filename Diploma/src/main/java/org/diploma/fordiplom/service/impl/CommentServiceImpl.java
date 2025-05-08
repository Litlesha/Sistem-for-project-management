package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.diploma.fordiplom.entity.CommentEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.CommentRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private  CommentRepository commentRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<CommentEntity> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Override
    public CommentEntity addComment(Long taskId, String text, String email) {
        UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setTask(task);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
