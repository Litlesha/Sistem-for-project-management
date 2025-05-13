package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.diploma.fordiplom.entity.CommentEntity;
import org.diploma.fordiplom.entity.DTO.CommentDTO;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.CommentRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.CommentService;
import org.diploma.fordiplom.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private  CommentRepository commentRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskHistoryService taskHistoryService;
    @Override
    public List<CommentDTO> getCommentsByTaskId(Long taskId) {
        List<CommentEntity> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        // Преобразуем сущности в DTO
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public CommentEntity addComment(Long taskId, String text, String email) {
        // Получаем пользователя по email
        UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Получаем задачу по ID
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Старое состояние комментариев задачи (до добавления нового)
        String oldComments = "";

        // Создание нового комментария
        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setTask(task);
        comment.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(Instant.now());

        // Сохраняем новый комментарий
        // Новое состояние комментариев задачи (с добавленным комментарием)
        String newComments = comment.getText();
        // Получаем email текущего пользователя (для истории)
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();


        // Записываем изменение в историю
        taskHistoryService.saveTaskHistory(
                task,
                oldComments,                 // Старое состояние комментариев
                newComments,                 // Новое состояние комментариев
                currentEmail,
                "create",                    // Тип действия
                "comments"                    // Поле, которое изменилось
        );
        return commentRepository.save(comment);
    }
}
