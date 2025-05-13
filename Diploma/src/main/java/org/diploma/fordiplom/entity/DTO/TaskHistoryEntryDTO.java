package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.ActionTypeEntity;
import org.diploma.fordiplom.entity.TaskHistoryEntry;

import java.time.LocalDateTime;
@Getter
@Setter
public class TaskHistoryEntryDTO {
    private Long id;
    private Long taskId;
    private String authorName;
    private String actionType;
    private String actionTypeName;
    private String field;
    private String fieldName;
    private String beforeValue;
    private String afterValue;
    private LocalDateTime createdAt;

    public TaskHistoryEntryDTO(TaskHistoryEntry entry) {
        this.id = entry.getId();
        this.taskId = entry.getTask().getId();
        this.authorName = entry.getAuthor() != null ? entry.getAuthor().getEmail() : null;

        // Извлекаем строковое представление для actionType
        this.actionType = entry.getActionType() != null ? entry.getActionType().getCode() : null;

        if ("update".equals(this.actionType)) {
            this.actionTypeName = "обновил";
        } else if ("create".equals(this.actionType)) {
            this.actionTypeName = "добавил";
        } else {
            this.actionTypeName = "удалил";
        }

        // Извлекаем строковое представление для поля
        this.field = entry.getField() != null ? entry.getField().getCode() : null;
        this.fieldName = entry.getField() != null ? entry.getField().getName() : null;

        this.beforeValue = entry.getBeforeValue();
        this.afterValue = entry.getAfterValue();
        this.createdAt = entry.getCreatedAt();
    }
}

