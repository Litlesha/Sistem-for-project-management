package org.diploma.fordiplom.service;


import org.aspectj.apache.bcel.generic.Tag;
import org.diploma.fordiplom.entity.DTO.TagDTO;
import org.diploma.fordiplom.entity.DTO.TaskDTO;
import org.diploma.fordiplom.entity.DTO.request.TaskRequest;
import org.diploma.fordiplom.entity.TagEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaskService {
    TaskEntity createTask(TaskRequest request);
    String keyGenerator(TaskRequest request);
    List<TaskEntity> getTasksBySprintId(Long sprintId);
    List<TaskEntity> getBackLogTasksByProjectId(Long projectId);
    void updateTaskLocation(Long taskId, Long sprintId);
    void updateStatus(Long taskId, String status);
    List<TaskDTO> searchTasksInSprint(String query, Long projectId, Long sprintId);
    TaskEntity getTaskById(Long taskId);
    TaskEntity updateTaskTitle(Long taskId, String taskTitle);
    TaskEntity updateTaskDescription(Long taskId, String taskDescription);
    TaskEntity updateTaskPriority(Long taskId, String newPriority);
    List<TagDTO> getTagsForTask(Long taskId);
    TagDTO addTagToTask(Long taskId, TagDTO tagDTO);
    void removeTagFromTask(Long taskId, Long tagId);
    List<TagDTO> searchTags(String query);
    void assignTeam(Long taskId, Long teamId);
    void assignExecutor(Long taskId, Long userId);

}
