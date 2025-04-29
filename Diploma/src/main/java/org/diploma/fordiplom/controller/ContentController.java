package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContentController {
    @Autowired
    ProjectService projectService;

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

        @GetMapping("/board")
    public String board(){
        return "board";
    }
    @GetMapping("/project_page")
    public String projectPage(@RequestParam Long id, @RequestParam String section) {
        // Логика для загрузки страницы в зависимости от секции
        if (section.equals("board")) {
            return "board";  // Рендерит страницу доски
        }
        return "project_page";  // Стандартная страница проекта
    }
}
