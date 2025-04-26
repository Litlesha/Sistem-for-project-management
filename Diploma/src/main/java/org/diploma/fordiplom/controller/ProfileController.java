package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class ProfileController {
    @Autowired
    private UserService userService;
    @GetMapping("/me")
    public UserEntity getCurrentUser(Principal principal) {
        UserEntity user = userService.getCurrentUser(principal);  // Получаем пользователя по email
        return user;
    }
    @PutMapping("/update-profile")
    public ResponseEntity<UserEntity> updateUserProfile(@RequestBody Map<String, String> updates, Principal principal) {
        try {
            String email = principal.getName();

            String position = updates.get("position");
            String username = updates.get("username");
            String organization = updates.get("organization");

            UserEntity updatedUser = userService.updateUserProfile(email, position, username, organization);

            return ResponseEntity.ok(updatedUser); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // В случае ошибки возвращаем статус 400
        }
    }
}

