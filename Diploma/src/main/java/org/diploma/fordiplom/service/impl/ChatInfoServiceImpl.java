package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.diploma.fordiplom.entity.*;
import org.diploma.fordiplom.entity.DTO.response.ChatInfoResponse;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.ChatInfoService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatInfoServiceImpl implements ChatInfoService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    TeamService teamService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public ChatInfoResponse getChatInfo(String type, Long id) {
        switch (type) {
            case "projectChat":
                ProjectEntity project = projectService.getProjectById(id);
                if (project == null) throw new EntityNotFoundException("Проект не найден");
                int projectMembers = project.getUsers().size();
                return new ChatInfoResponse("Чат проекта: " + project.getName(), projectMembers);

            case "teamChat":
                TeamEntity team = teamService.getTeamById(id);
                if (team == null) throw new EntityNotFoundException("Команда не найдена");
                int teamMembers = team.getEmails().size();
                return new ChatInfoResponse("Чат команды: " + team.getTeam_name(), teamMembers);

            case "sprintChat":
                SprintEntity sprint = sprintService.getSprintById(id);
                if (sprint == null) throw new EntityNotFoundException("Спринт не найден");

                // Получение задач этого спринта
                List<TaskEntity> tasks = taskRepository.findBySprintId(sprint.getId());

                // Уникальные исполнители
                Set<UserEntity> uniqueExecutors = tasks.stream()
                        .map(TaskEntity::getExecutor)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                int sprintMembers = uniqueExecutors.size();

                return new ChatInfoResponse("Чат спринта: " + sprint.getSprintName(), sprintMembers);

            default:
                throw new IllegalArgumentException("Неверный тип чата: " + type);
        }
    }
}
