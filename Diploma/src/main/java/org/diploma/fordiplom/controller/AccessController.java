package org.diploma.fordiplom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessController {
    @GetMapping("/access-project-denied")
    public String accessProjectDenied(){
        return "access_project_denied";
    }
}
