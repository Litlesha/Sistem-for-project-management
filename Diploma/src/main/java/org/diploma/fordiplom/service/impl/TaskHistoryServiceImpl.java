package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.diploma.fordiplom.entity.*;
import org.diploma.fordiplom.entity.DTO.TaskHistoryEntryDTO;
import org.diploma.fordiplom.repository.*;
import org.diploma.fordiplom.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskHistoryServiceImpl implements TaskHistoryService {
    @Autowired
    private TaskHistoryEntryRepository historyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActionTypeRepository actionTypeRepository;
    @Autowired
    private FieldTypeRepository fieldTypeRepository;

    @Override
    public List<TaskHistoryEntryDTO> getHistoryForTask(Long taskId) {
        return historyRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(TaskHistoryEntryDTO::new)
                .collect(Collectors.toList());
    }
    @Override
    public TaskHistoryEntry saveTaskHistory(TaskEntity task, String oldValue, String newValue,
                                            String email, String actionCode, String fieldCode) {
        UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        ActionTypeEntity actionType = actionTypeRepository.findByCode(actionCode)
                .orElseThrow(() -> new EntityNotFoundException("Тип действия 'update' не найден"));

        FieldTypeEntity fieldType = fieldTypeRepository.findByCode(fieldCode)
                .orElseThrow(() -> new EntityNotFoundException("Поле не найдено"));

        TaskHistoryEntry historyEntry = new TaskHistoryEntry();
        historyEntry.setTask(task);
        historyEntry.setAuthor(author);
        historyEntry.setActionType(actionType);
        historyEntry.setField(fieldType);
        historyEntry.setBeforeValue(oldValue.isEmpty() ? "Нет" : oldValue);
        historyEntry.setAfterValue(newValue);
        historyEntry.setCreatedAt(LocalDateTime.now());

        return historyRepository.save(historyEntry);
    }
}
