package org.diploma.fordiplom.service.impl;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.*;
import org.diploma.fordiplom.entity.DTO.TagDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.TaskPositionUpdateRequest;
import org.diploma.fordiplom.entity.DTO.request.TaskRequest;
import org.diploma.fordiplom.repository.*;
import org.diploma.fordiplom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskHistoryService taskHistoryService;

    @Override
    public TaskEntity createTask(TaskRequest request){
        TaskEntity task = new TaskEntity();
        task.setTitle(request.getTitle());
        task.setTaskType(request.getTask_type());
        task.setTaskKey(keyGenerator(request));
        task.setStatus(request.getStatus());
        task.setCreatedAt(Instant.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity author = userService.getUserByEmail(email);
        task.setAssignedUser(author);
        if (request.getSprintId() != null) {
            SprintEntity sprint = sprintService.getSprintById(request.getSprintId());
            task.setSprint(sprint);
        } else {
            task.setSprint(null); // Явно укажем null, чтобы было понятно
        }

        task.setProject(projectService.getProjectById(request.getProjectId()));
        return taskRepository.save(task);
    }

    @Override
    public Long extractProjectIdFromTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(TaskEntity::getSprint)
                .map(SprintEntity::getProject)
                .map(ProjectEntity::getId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }


    @Override
    public String keyGenerator(TaskRequest request) {
        String projectKey = projectService.getProjectKey(request.getProjectId());

        if (projectKey == null || projectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ проекта не найден для ID: " + request.getProjectId());
        }

        List<String> taskKeys = taskRepository.findTaskKeysByProjectKey(projectKey);
        int nextNumber = 1;

        if (!taskKeys.isEmpty()) {
            String lastKey = taskKeys.get(0); // Самый последний по номеру
            String[] parts = lastKey.split("-");
            if (parts.length == 2) {
                try {
                    nextNumber = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    // Если вдруг формат битый
                    nextNumber = 1;
                }
            }
        }

        return projectKey + "-" + nextNumber;
    }

    @Override
    public List<TaskEntity> getTasksBySprintId(Long sprintId) {
        return taskRepository.findBySprintIdAndIsCompletedFalse(sprintId);
    }
    @Override
    public List<TaskDTO> getTaskDTOsBySprintId(Long sprintId) {
        List<TaskEntity> tasks = taskRepository.findBySprintIdAndIsCompletedFalse(sprintId);
        return tasks.stream()
                .map(TaskDTO::new) // используем конструктор
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskEntity> getBackLogTasksByProjectId(Long projectId){
        return taskRepository.findByProject_IdAndSprintIsNullOrderByPositionAsc(projectId);
    }

    @Override
    @Transactional
    public void updateTaskLocation(Long taskId, Long sprintId, Integer newPosition) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        // Удаляем задачу из текущего списка (обнуляем позицию, чтобы не конфликтовала при переиндексации)
        Integer oldPosition = task.getPosition();
        SprintEntity oldSprint = task.getSprint();

        // Получаем целевой спринт (или null для бэклога)
        SprintEntity newSprint = null;
        if (sprintId != null) {
            newSprint = sprintRepository.findById(sprintId)
                    .orElseThrow(() -> new RuntimeException("Спринт не найден"));
        }

        // Обновляем позиции всех задач в целевом спринте (или в бэклоге)
        List<TaskEntity> tasksInTarget = taskRepository.findBySprintOrderByPositionAsc(newSprint);

        tasksInTarget.removeIf(t -> t.getId().equals(taskId)); // исключаем текущую задачу, если она уже в списке

        // Вставляем задачу в нужную позицию
        if (newPosition < 0 || newPosition > tasksInTarget.size()) {
            newPosition = tasksInTarget.size();
        }
        tasksInTarget.add(newPosition, task);

        // Переустанавливаем позиции всем задачам
        for (int i = 0; i < tasksInTarget.size(); i++) {
            TaskEntity t = tasksInTarget.get(i);
            t.setPosition(i);
        }

        task.setSprint(newSprint);
        task.setUpdatedAt(Instant.now());
        taskRepository.saveAll(tasksInTarget); // сохраняем все задачи с обновленными позициями
    }

    @Override
    public void updateStatus(Long taskId, String newStatus) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        String oldStatus = task.getStatus(); // сохраняем старый статус

        task.setStatus(newStatus);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);

        // Получаем email текущего пользователя
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Добавляем в историю
        taskHistoryService.saveTaskHistory(
                task,
                oldStatus,
                newStatus,
                email,
                "update",         // действие
                "status"          // поле
        );
    }
    /*public List<TaskDTO> searchTasksInSprint(String query, Long projectId, Long sprintId) {
        // Получаем список задач из репозитория
        List<TaskEntity> tasks = taskRepository.searchInSprint(query, projectId, sprintId);

        // Преобразуем список TaskEntity в TaskDTO
        return tasks.stream()
                .map(TaskDTO::new)  // Используем конструктор TaskDTO, который принимает TaskEntity
                .collect(Collectors.toList());
    }*/

    @Override
    public void assignSprint(Long taskId, Long sprintId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        SprintEntity sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Спринт не найден"));

        task.setSprint(sprint);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }
    public TaskDTO getTaskByKey(String key) {
        return taskRepository.findByKey(key);
    }

    @Override
    public List<TaskDTO> searchTasksInActiveSprints(String query, Long projectId) {
        // Получаем ID всех активных спринтов проекта
        List<Long> activeSprintIds = sprintRepository.findActiveSprintIdsByProjectId(projectId);

        if (activeSprintIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Ищем задачи в этих спринтах
        List<TaskEntity> tasks = taskRepository.searchInSprints(query, projectId, activeSprintIds);

        return tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public TaskEntity getTaskById(Long id) {
       return taskRepository.findById(id).get();
    }

    @Override
    public TaskEntity updateTaskTitle(Long taskId, String taskTitle) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена с id " + taskId));

        String oldTitle = task.getTitle();
        String newTitle = taskTitle;

        // Если название не изменилось, ничего не делать
        if (Objects.equals(oldTitle, newTitle)) {
            return task;
        }

        task.setTitle(newTitle);
        task.setUpdatedAt(Instant.now());
        TaskEntity savedTask = taskRepository.save(task);

        // actionType: create/update (в зависимости от наличия старого значения)
        String actionType = (oldTitle == null || oldTitle.isBlank()) ? "create" : "update";

        // Подставь email текущего пользователя
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        taskHistoryService.saveTaskHistory(savedTask, oldTitle, newTitle, email, actionType, "title");

        return savedTask;
    }


    @Override
    public TaskEntity updateTaskDescription(Long taskId, String taskDescription) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена с id " + taskId));

        String oldDescription = task.getDescription();
        if (oldDescription == null) {
            oldDescription = "";
        }
        String newDescription = taskDescription;

        if (Objects.equals(oldDescription, newDescription)) {
            return task;
        }

        task.setDescription(newDescription);
        task.setUpdatedAt(Instant.now());
        TaskEntity savedTask = taskRepository.save(task);

        String actionType = (oldDescription == null || oldDescription.isBlank()) ? "create" : "update";
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        taskHistoryService.saveTaskHistory(savedTask, oldDescription, newDescription, email, actionType, "description");

        return savedTask;
    }

    public TaskEntity updateTaskPriority(Long taskId, String newPriority, String email) {
        TaskEntity task = getTaskById(taskId);
        String oldPriority = task.getPriority();

        if (oldPriority == null) {
            oldPriority = "";
        }

        // Если приоритет ещё не был установлен, то это первый раз
        String actionType = oldPriority.isEmpty() ? "create" : "update";

        if (!oldPriority.equals(newPriority)) {
            task.setPriority(newPriority);
            task.setUpdatedAt(Instant.now());
            task = taskRepository.save(task);

            // Используем выделенный метод для сохранения истории
            taskHistoryService.saveTaskHistory(task, oldPriority, newPriority, email, actionType, "priority");
        }

        return task;
    }

    @Transactional
    public TagDTO addTagToTask(Long taskId, TagDTO tagDTO) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TagEntity tagEntity = tagRepository.findByName(tagDTO.getName()).orElse(null);
        if (tagEntity == null) {
            tagEntity = new TagEntity();
            tagEntity.setName(tagDTO.getName());
            tagEntity = tagRepository.save(tagEntity);
        }

        if (!task.getTags().contains(tagEntity)) {
            task.getTags().add(tagEntity);
            task.setUpdatedAt(Instant.now());
            taskRepository.save(task);

            // ✅ История
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            taskHistoryService.saveTaskHistory(
                    task,
                    "",                             // oldValue
                    tagEntity.getName(),            // newValue
                    email,
                    "create",                          // actionCode
                    "tags"                           // fieldCode
            );
        }

        return new TagDTO(tagEntity.getId(), tagEntity.getName());
    }


    // Метод для удаления метки из задачи
    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TagEntity tagEntity = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (task.getTags().contains(tagEntity)) {
            task.getTags().remove(tagEntity);
            task.setUpdatedAt(Instant.now());
            taskRepository.save(task);

            // ✅ История
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            taskHistoryService.saveTaskHistory(
                    task,
                    tagEntity.getName(),            // oldValue
                    "",                             // newValue
                    email,
                    "delete",                       // actionCode
                    "tags"                           // fieldCode
            );
        }
    }

    @Override
    public List<TagDTO> searchTags(String query) {
        List<TagEntity> tagEntities = tagRepository.findByNameContainingIgnoreCase(query);

        // Преобразуем сущности в DTO
        return tagEntities.stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void assignTeam(Long taskId, Long teamId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Команда не найдена"));

        String oldTeamName = task.getTeam() != null ? task.getTeam().getTeam_name() : ""; // Сохраняем старое значение команды
        String newTeamName = team.getTeam_name(); // Новое значение команды

        task.setTeam(team);
        task.setUpdatedAt(Instant.now());
        task = taskRepository.save(task);
        String actionType = oldTeamName.isEmpty() ? "create" : "update";
        // Проверяем, изменилась ли команда
        if (!oldTeamName.equals(newTeamName)) {
            // Добавляем запись в историю
            String email = SecurityContextHolder.getContext().getAuthentication().getName(); // Получаем email пользователя
            taskHistoryService.saveTaskHistory(
                    task,
                    oldTeamName,               // Старое значение (команда до изменений)
                    newTeamName,               // Новое значение (команда после изменений)
                    email,
                    actionType,                  // Тип действия
                    "team"                     // Поле, которое было изменено
            );
        }
    }
    @Override
    public void assignExecutor(Long taskId, Long userId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String oldExecutorEmail = task.getExecutor() != null ? task.getExecutor().getEmail() : ""; // Старое значение исполнителя
        String newExecutorEmail = user.getEmail(); // Новое значение исполнителя

        task.setExecutor(user);
        task.setUpdatedAt(Instant.now());
        task = taskRepository.save(task);
        String actionType = oldExecutorEmail.isEmpty() ? "create" : "update";
        // Проверяем, изменился ли исполнитель
        if (!oldExecutorEmail.equals(newExecutorEmail)) {
            // Добавляем запись в историю
            String email = SecurityContextHolder.getContext().getAuthentication().getName(); // Получаем email пользователя
            taskHistoryService.saveTaskHistory(
                    task,
                    oldExecutorEmail,               // Старое значение (исполнитель до изменений)
                    newExecutorEmail,               // Новое значение (исполнитель после изменений)
                    email,
                    actionType,                       // Тип действия
                    "executor"                      // Поле, которое было изменено
            );
        }
    }

    @Override
    public void updateTaskPositions(List<TaskPositionUpdateRequest> updates) {
        for (TaskPositionUpdateRequest update : updates) {
            TaskEntity task = taskRepository.findById(update.getTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

            task.setPosition(update.getPosition());
            task.setUpdatedAt(Instant.now());
            taskRepository.save(task);
        }
    }

    // Метод для получения всех меток задачи
    @Override
    public List<TagDTO> getTagsForTask(Long taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return task.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }
}

