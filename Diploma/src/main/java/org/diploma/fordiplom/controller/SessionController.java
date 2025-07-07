package org.diploma.fordiplom.controller;

import jakarta.servlet.http.HttpSession;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping("/selectProject")
    public Map<String, String> selectProject(@RequestParam Long projectId, HttpSession session) {
        session.setAttribute("projectId", projectId); // Сохраняем выбранный projectId в сессии
        return Map.of("projectId", projectId.toString());
    }

    @PostMapping("/selectTeam")
    public Map<String, String> selectTeam(@RequestParam Long teamId, HttpSession session) {
        session.setAttribute("projectId", teamId); // Сохраняем выбранный projectId в сессии
        return Map.of("projectId", teamId.toString());
    }

    @PostMapping("/selectSprint")
    public Map<String, String> selectSprint(@RequestParam Long sprintId, HttpSession session) {
        session.setAttribute("projectId", sprintId); // Сохраняем выбранный projectId в сессии
        return Map.of("projectId", sprintId.toString());
    }

    @GetMapping("/info")
    public Map<String, Object> currentUser(Authentication authentication, HttpSession session) {
        String username = authentication.getName();

        Long projectId = (Long) session.getAttribute("projectId");

        if (projectId == null) {
            List<ProjectEntity> projects = projectRepository.findByUserEmail(username);

            projectId = projects.isEmpty() ? null : projects.get(0).getId();

            if (projectId != null) {
                session.setAttribute("projectId", projectId);
            }
        }
        return Map.of(
                "username", username,
                "projectId", projectId != null ? projectId : "defaultProject"
        );
    }
}
