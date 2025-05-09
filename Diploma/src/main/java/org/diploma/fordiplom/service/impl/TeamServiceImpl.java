package org.diploma.fordiplom.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.TeamEntity;
import org.diploma.fordiplom.entity.DTO.request.TeamRequest;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.TeamRepository;

import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.TeamService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserService userService;

    public TeamEntity createTeam(TeamRequest request, String creatorEmail) {
        UserEntity creator = userService.getUserByEmail(creatorEmail);
        if (creator == null) {
            throw new IllegalArgumentException("Создатель команды не найден");
        }

        TeamEntity newTeam = new TeamEntity();
        newTeam.setTeam_name(request.getTeam_name());
        newTeam.setDescription(request.getDescription());

        Set<UserEntity> members = new HashSet<>();
        members.add(creator);
        for (String email : request.getEmails()) {
            UserEntity member = userService.getUserByEmail(email);
            if (member != null) {
                members.add(member);
            } else {
                System.out.println("Пользователь с email " + email + " не найден");
            }
        }

        newTeam.setEmails(members); // или как называется у тебя поле пользователей
        return teamRepository.save(newTeam);
    }
    @Override
    public TeamEntity getTeamById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Team not found"));
    }

    @Override
    public List<TeamEntity> getTeamsByUserEmail(String email) {
        return teamRepository.findByEmail(email);
    }

    @Override
    public TeamEntity editTeam(Long id, TeamRequest request) {
        TeamEntity editTeam = teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Team not found"));
        editTeam.setTeam_name(request.getTeam_name());
        editTeam.setDescription(request.getDescription());
        Set<UserEntity> members = new HashSet<>();
        for (String email : request.getEmails()) {
            UserEntity member = userService.getUserByEmail(email);
            if (member != null) {
                members.add(member);
            } else {
                System.out.println("Пользователь с email " + email + " не найден");
            }
        }

        editTeam.setEmails(members);
        return teamRepository.save(editTeam);
    }

    @Override
    public void saveteamImgPath(Long teamId, String imgUrl) {
        TeamEntity team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        team.setTeamImgPath(imgUrl);
        teamRepository.save(team);  // Сохраняем команду с обновленным путем к изображению
    }

    @Override
    public void updateTeamName(Long teamId, String newName) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        team.setTeam_name(newName);
        teamRepository.save(team);
    }

    @Transactional
    @Override
    public void addMembers(Long teamId, List<String> emails) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Команда не найдена"));

        List<UserEntity> usersToAdd = userService.getAllUsersByEmails(emails);
        if (usersToAdd.isEmpty()) {
            throw new IllegalArgumentException("Пользователи не найдены");
        }

        team.getEmails().addAll(usersToAdd);
        teamRepository.save(team);
    }

    @Transactional
    public void leaveTeam(Long teamId, UserEntity user) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Команда не найдена"));

        if (!team.getEmails().contains(user)) {
            throw new RuntimeException("Вы не являетесь участником этой команды");
        }

        team.getEmails().remove(user);
    }

    @Override
    public void deleteTeam(Long teamId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Команда не найдена"));

        teamRepository.delete(team);
    }

    @Override
    public List<TeamEntity> searchTeams(String teamName) {
        return teamRepository.findByTeamNameContainingIgnoreCase(teamName);
    }

    @Override
    public List<TeamEntity> getUserTeams(String email){return teamRepository.findByEmail(email);}


}
