package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class ContentController {
    @Autowired
    ProjectService projectService;
    @Autowired
    TeamService teamService;
    @Autowired
    SprintService sprintService;
    @Autowired
    private PURService pURService;
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "hello";
    }

    @GetMapping("/register")
    public String registrtion() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/team_page")
    public String teamPage() {
        return "team_page";
    }

//        @GetMapping("/project_page")
//    public String projectPage() {
//            return "project_page";}
    @GetMapping("projects")
    public String projects() {
        return "projects";
    }

    @GetMapping("teams")
    public String teams() {
        return "teams";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/task")
    public String task(@RequestParam String key) {
        return "task";
    }

    @GetMapping("/board")
    public String board(){
        return "board";
    }

    @GetMapping("/project_page")
    public String projectPage(
            @RequestParam Long id,
            @RequestParam String section,
            @RequestParam(required = false) Long team,
            @RequestParam(required = false) Long sprint,
            Model model,
            Principal principal
    ) {
        String email = principal.getName();


        Long entityId;
        if ("teamChat".equals(section) && team != null) {
            entityId = team;
        } else if ("sprintChat".equals(section) && sprint != null) {
            entityId = sprint;
        } else {
            entityId = id;
        }

        boolean accessAllowed = switch (section) {
            case "backlog", "board", "sprintChatList", "teamChatList" -> projectService.isUserInProject(email, id);
            default -> true;
        };

        boolean reportAllowed = switch (section) {
            case "report" -> pURService.checkAccess(id, userService.getUserByEmail(email).getId_user());
            default -> true;
        };


        boolean chatAccessAllowed = switch (section) {
            case "teamChat" -> teamService.isUserInTeam(email, entityId);
            case "projectChat" -> projectService.isUserInProject(email, id);
            case "sprintChat" -> sprintService.isUserInSprint(email, entityId);
            default -> true;
        };

        if(!reportAllowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!chatAccessAllowed) {
            return "/chat_access_denied";
        }

        if (!accessAllowed) {
            return "/access_project_denied";
        }

        model.addAttribute("chatId", section);
        model.addAttribute("entityId", entityId);
        model.addAttribute("userTeams", teamService.getTeamsByUserEmail(email));
        model.addAttribute("userSprints", sprintService.getActiveSprintsForUser(id, email));

        switch (section) {
            case "sprintChatList":
                return "sprintChatList";
            case "board":
                return "board";
            case "teamChatList":
                return "teamChatList";
            case "report":
                return "report";
            case "code":
                return "code";
            case "projectChat":
            case "teamChat":
            case "sprintChat":
                return "chat";

            default:
                return "project_page";
        }
    }
}
