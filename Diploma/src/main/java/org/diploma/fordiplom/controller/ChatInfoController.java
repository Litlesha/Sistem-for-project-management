package org.diploma.fordiplom.controller;

import jakarta.persistence.EntityNotFoundException;
import org.diploma.fordiplom.entity.DTO.response.ChatInfoResponse;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.TeamEntity;
import org.diploma.fordiplom.service.ChatInfoService;
import org.diploma.fordiplom.service.ProjectService;
import org.diploma.fordiplom.service.SprintService;
import org.diploma.fordiplom.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatInfoController {
    @Autowired
    private ChatInfoService chatInfoService;

    @GetMapping("/info")
    public ResponseEntity<?> getChatInfo(@RequestParam String type, @RequestParam Long id) {
        try {
            ChatInfoResponse info = chatInfoService.getChatInfo(type, id);
            return ResponseEntity.ok(info);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

