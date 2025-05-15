package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.DTO.ProjectDTO;
import org.diploma.fordiplom.entity.DTO.ProjectSummaryDTO;
import org.diploma.fordiplom.entity.DTO.TeamDTO;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.TeamEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.ProjectRepository;
import org.diploma.fordiplom.repository.SprintRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.TaskService;
import org.diploma.fordiplom.service.TeamService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.diploma.fordiplom.entity.DTO.request.ProjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private SprintRepository sprintRepository;

    public ProjectEntity createProject(ProjectRequest request, String creatorEmail) {
        UserEntity creator = userService.getUserByEmail(creatorEmail);
        if (creator == null) {
            throw new IllegalArgumentException("Создатель проекта не найден");
        }
        ProjectEntity project = new ProjectEntity();
        project.setName(request.getName());
        project.setKey(request.getKey());
        project.setDescription(request.getDescription());
        project.setCreatedAt(LocalDate.now());
        Set<UserEntity> users = new HashSet<>();
        users.add(creator); // добавляем создателя
        project.setUsers(users);
        return projectRepository.save(project);
    }

    @Override
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public String getProjectKey(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return project.getKey();
    }

    @Override
    @Transactional
    public void addTeamToProject(Long projectId, Long teamId) {
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            TeamEntity team = teamService.getTeamById(teamId);
            project.getTeams().add(team);
            project.addUsersFromTeam(team);

            projectRepository.save(project);
        }

    @Override
    public List<TeamDTO> getTeamsByProjectId(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getTeams().stream()
                .map(team -> new TeamDTO(team.
                        getId_team(), team.getTeam_name()))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO getProjectByTaskId(Long taskId) {
        // Находим задачу по ID
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        // Получаем проект по ID задачи
        ProjectEntity project = task.getProject(); // Предполагается, что у задачи есть ссылка на проект

        // Создаем ProjectDTO для отправки
        return new ProjectDTO(project.getId(), project.getName());
    }

    @Override
    public ProjectSummaryDTO getProjectSummary(Long projectId) {
        // 1. Получить проект, убедиться, что он существует

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        // 2. Посчитать количество завершённых спринтов для проекта
        int completedSprintsCount = sprintRepository.countByProjectIdAndIsCompletedTrue(projectId);

        // 3. Посчитать количество выполненных задач по всем спринтам проекта
        int completedTasksCount = taskRepository.countByProjectIdAndIsCompletedTrue(projectId);

        // 4. Рассчитать длительность проекта в днях (например, по датам создания и текущей дате)
        long projectDurationDays = Duration.between(project.getCreatedAt().atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();

        return new ProjectSummaryDTO(completedSprintsCount, completedTasksCount, projectDurationDays);
    }

    @Override
    public void completeProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));
        project.setIsDeleted(true);
        projectRepository.save(project);
    }


    @Override
    public List<ProjectEntity> getProjectsByUserEmail(String email) {
        return projectRepository.findByUserEmail(email);
    }

}
