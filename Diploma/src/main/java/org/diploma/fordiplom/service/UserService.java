package org.diploma.fordiplom.service;

import jakarta.mail.MessagingException;
import org.diploma.fordiplom.entity.DTO.UserDTO;
import org.diploma.fordiplom.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface UserService {
        UserEntity createUser(UserEntity user) throws Exception;

        UserEntity updateUser(String email, UserEntity user);

        UserEntity getUserByEmail(String email);
        List<UserEntity> getAllUsersByEmails(List<String> emails);
        UserEntity findUserById(Long id);
        UserEntity getCurrentUser(Principal principal);
        boolean existsByEmail(String email);
        UserEntity updateUserProfile(String email, String position,String username, String organization);
        void saveUserImgPath(Long userId, String imgUrl);
        void sendConfirmationEmail(String toEmail, String token) throws MessagingException;
        String confirmUser(String token);
        List<UserEntity> getUsersByProjectTasks(Long projectId);
        List<UserDTO> getUsersWithTasksByProjectId(Long projectId);
        UserDTO getUserWithTasks(Long userId, Long projectId);
        List<UserEntity> getUsersForProject(Long projectId);
        String getGitHubToken(String email);
}