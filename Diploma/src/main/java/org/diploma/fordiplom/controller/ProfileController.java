package org.diploma.fordiplom.controller;

import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.Map;
import java.util.UUID;

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
    @PostMapping("/api/user/upload-photo")
    public ResponseEntity<Map<String, String>> uploadUserPhoto(@RequestParam("image") MultipartFile imageFile,
                                                               @RequestParam Long userId) throws IOException {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл пустой"));
        }

        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());

        // Абсолютный путь
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
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
        userService.saveUserImgPath(userId, imageUrl);  // 🆕 Сохраняем путь в БД

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}

