package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface UserService {
        public UserEntity createUser(UserEntity user) throws Exception;

        public UserEntity updateUser(String email, UserEntity user);

        public UserEntity getUserByEmail(String email);
        List<UserEntity> getAllUsersByEmails(List<String> emails);
        public UserEntity findUserById(Long id);
        public UserEntity getCurrentUser(Principal principal);
        public boolean existsByEmail(String email);
        UserEntity updateUserProfile(String email, String position,String username, String organization);
        void saveUserImgPath(Long userId, String imgUrl);
}