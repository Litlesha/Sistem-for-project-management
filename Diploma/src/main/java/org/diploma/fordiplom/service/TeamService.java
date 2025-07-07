package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.TeamEntity;
import org.diploma.fordiplom.entity.DTO.request.TeamRequest;
import org.diploma.fordiplom.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeamService {
    public List<TeamEntity> getUserTeams(String email);
    TeamEntity getTeamById(Long id);
    public TeamEntity createTeam(TeamRequest request, String creatorEmail);
    List<TeamEntity> getTeamsByUserEmail(String email);
    TeamEntity editTeam(Long id, TeamRequest request);
    void saveteamImgPath(Long teamId, String imgUrl);
    void updateTeamName(Long teamId, String newName);
    void addMembers(Long teamId, List<String> emails);
    void leaveTeam(Long teamId, UserEntity user);
    boolean isUserInTeam(String email, Long teamId);
    void deleteTeam(Long teamId);
    List<TeamEntity> searchTeams(String teamName);
    List<UserEntity> getUsersForTeam(Long teamId);
}
