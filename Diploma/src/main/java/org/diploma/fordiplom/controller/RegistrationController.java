package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class RegistrationController {

    @Autowired
    UserService userService;
    @PostMapping(value = "/register")
    public UserEntity register(@RequestBody UserEntity user) throws Exception {
        return userService.createUser(user);
    }
    @GetMapping("/confirm")
    public Object confirmEmail(@RequestParam("token") String token) {
        String result = userService.confirmUser(token);
        if (result.startsWith("Некорректный")) {
            return ResponseEntity.badRequest().body(result);
        }

        return new RedirectView("/login");
    }
}
