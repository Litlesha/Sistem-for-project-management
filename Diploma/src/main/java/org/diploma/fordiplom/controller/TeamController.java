package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.config.MyUserDetailService;
import org.diploma.fordiplom.entity.TeamEntity;
import org.diploma.fordiplom.entity.DTO.request.TeamRequest;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.TeamRepository;
import org.diploma.fordiplom.service.TeamService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TeamController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private UserService userService;
    @Autowired
    private TeamRepository teamRepository;

    @PostMapping(path = "/create_team", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamEntity> createTeam(@RequestBody TeamRequest teamRequest, Principal principal) {
        String creatorEmail = principal.getName(); // или откуда ты получаешь текущего юзера
        TeamEntity team = teamService.createTeam(teamRequest, creatorEmail);
        return ResponseEntity.ok(team);
    }
    @GetMapping("/api/team/{id}")
    public TeamEntity getTeamById(@PathVariable long id) {
        TeamEntity team = teamService.getTeamById(id);
        if (team == null) {
            throw new IllegalArgumentException("Team not found");
        }
        return team;
    }
    @GetMapping("/api/teams")
    public List<TeamEntity> getUserTeams(Principal principal) {
        String email = principal.getName();
        return teamService.getTeamsByUserEmail(email);
    }
    @PutMapping("/api/editteam/{id}")
    public ResponseEntity<TeamEntity> updateTeam(@PathVariable long id, @RequestBody TeamRequest teamRequest) {
        teamService.editTeam(id, teamRequest);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/api/team/upload-image")
    public ResponseEntity<Map<String, String>> uploadTeamImage(@RequestParam("image") MultipartFile imageFile,
                                                               @RequestParam Long teamId) throws IOException {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл пустой"));
        }

        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());

        // Абсолютный путь
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads"); // Используем абсолютный путь
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);


        try {
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось загрузить файл: " + e.getMessage()));
        }

        // Сохраняем путь изображения в БД
        String imageUrl = "/uploads/" + fileName;
        teamService.saveteamImgPath(teamId, imageUrl);  // Сохраняем в БД

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
    @PutMapping("/api/editteamname/{teamId}")
    public ResponseEntity<Void> editTeamName(@PathVariable Long teamId, @RequestBody Map<String, String> payload) {
        String newName = payload.get("team_name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        teamService.updateTeamName(teamId, newName.trim());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/api/team/{teamId}/add-members")
    public ResponseEntity<?> addMembersToTeam(@PathVariable Long teamId, @RequestBody List<String> emails) {
        try {
            teamService.addMembers(teamId, emails);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/team/{id}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable("id") Long teamId, Principal principal) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        teamService.leaveTeam(teamId, currentUser);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/api/team/{id}/delete")
    public ResponseEntity<?> deleteTeam(@PathVariable("id") Long teamId) {
        try {
            // Удаляем команду через сервис
            teamService.deleteTeam(teamId);
            return ResponseEntity.ok().build(); // Возвращаем успешный ответ
        } catch (Exception e) {
            // Обрабатываем возможные ошибки
            return ResponseEntity.status(500).body("Ошибка при удалении команды");
        }
    }
    @GetMapping("/api/teams/search")
    public ResponseEntity<List<TeamEntity>> searchTeams(@RequestParam String query) {
        List<TeamEntity> teams = teamService.searchTeams(query);
        return ResponseEntity.ok(teams);  // Возвращаем найденные команды
    }
    @GetMapping("/api/teams/{teamId}/users")
    public ResponseEntity<List<UserEntity>> getUsersForTeam(@PathVariable Long teamId) {
        List<UserEntity> users = teamService.getUsersForTeam(teamId);
        return ResponseEntity.ok(users); // Возвращаем список пользователей
    }
}
